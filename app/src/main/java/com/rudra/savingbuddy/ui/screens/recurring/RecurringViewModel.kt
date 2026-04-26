package com.rudra.savingbuddy.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.*
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class RecurringItem(
    val id: Long,
    val name: String,
    val amount: Double,
    val type: String,
    val category: String,
    val interval: RecurringInterval,
    val nextDate: Long,
    val notes: String?,
    val status: RecurringStatus = RecurringStatus.ACTIVE,
    val endCondition: EndCondition = EndCondition.NEVER,
    val endDate: Long? = null,
    val occurrencesRemaining: Int? = null,
    val pausedUntil: Long? = null,
    val isVariableAmount: Boolean = false,
    val averageAmount: Double? = null,
    val categorySplit: Map<String, Double>? = null,
    val isNeedsApproval: Boolean = false
)

data class RecurringSuggestion(
    val name: String,
    val amount: Double,
    val type: String,
    val category: String,
    val confidence: Float,
    val occurrences: Int,
    val avgDaysBetween: Int
)

data class RecurringUiState(
    val recurringItems: List<RecurringItem> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val selectedItems: Set<Long> = emptySet(),
    val isBulkMode: Boolean = false,
    val suggestions: List<RecurringSuggestion> = emptyList(),
    val showCalendarView: Boolean = false,
    val upcomingTransactions: List<UpcomingTransaction> = emptyList(),
    val totalMonthlyIncome: Double = 0.0,
    val totalMonthlyExpense: Double = 0.0,
    val commitmentsTotal: Double = 0.0,
    val warnings: List<String> = emptyList()
)

data class UpcomingTransaction(
    val date: Long,
    val items: List<RecurringItem>
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    private var allIncomes: List<Income> = emptyList()
    private var allExpenses: List<Expense> = emptyList()

    init {
        loadRecurringItems()
    }

    private fun loadRecurringItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                incomeRepository.getAllIncome(),
                expenseRepository.getAllExpenses()
            ) { incomeList, expenseList ->
                allIncomes = incomeList
                allExpenses = expenseList
                processRecurringData(incomeList, expenseList)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun processRecurringData(incomeList: List<Income>, expenseList: List<Expense>): RecurringUiState {
        val recurringItems = mutableListOf<RecurringItem>()
        val now = System.currentTimeMillis()
        
        // Process recurring income
        incomeList.filter { it.isRecurring }.forEach { income ->
            recurringItems.add(createRecurringItem(income, "Income"))
        }
        
        // Process recurring expenses
        expenseList.filter { it.isRecurring }.forEach { expense ->
            recurringItems.add(createRecurringItem(expense, "Expense"))
        }
        
        // Sort by next date
        val sortedItems = recurringItems.sortedBy { it.nextDate }
        
        // Calculate totals
        val monthlyIncome = sortedItems.filter { it.type == "Income" && it.status == RecurringStatus.ACTIVE }
            .sumOf { calculateMonthlyAmount(it) }
        val monthlyExpense = sortedItems.filter { it.type == "Expense" && it.status == RecurringStatus.ACTIVE }
            .sumOf { calculateMonthlyAmount(it) }
        
        // Find suggestions from non-recurring transactions
        val suggestions = analyzePatterns(incomeList, expenseList)
        
        // Get upcoming transactions for calendar
        val upcoming = getUpcomingTransactions(sortedItems)
        
        // Generate warnings
        val warnings = generateWarnings(upcoming)
        
        return RecurringUiState(
            recurringItems = sortedItems,
            isLoading = false,
            suggestions = suggestions,
            upcomingTransactions = upcoming,
            totalMonthlyIncome = monthlyIncome,
            totalMonthlyExpense = monthlyExpense,
            commitmentsTotal = monthlyExpense,
            warnings = warnings
        )
    }

    private fun createRecurringItem(income: Income, type: String): RecurringItem {
        val interval = income.recurringInterval ?: RecurringInterval.MONTHLY
        return RecurringItem(
            id = income.id,
            name = income.source.ifBlank { income.category.displayName },
            amount = income.amount,
            type = type,
            category = income.category.displayName,
            interval = interval,
            nextDate = calculateNextDate(income.date, interval),
            notes = income.notes,
            isVariableAmount = false,
            averageAmount = income.amount
        )
    }

    private fun createRecurringItem(expense: Expense, type: String): RecurringItem {
        return RecurringItem(
            id = expense.id,
            name = expense.category.displayName,
            amount = expense.amount,
            type = type,
            category = expense.category.displayName,
            interval = RecurringInterval.MONTHLY,
            nextDate = calculateNextDate(expense.date, RecurringInterval.MONTHLY),
            notes = expense.notes,
            isVariableAmount = false,
            averageAmount = expense.amount
        )
    }

    private fun calculateNextDate(lastDate: Long, interval: RecurringInterval): Long {
        val calendar = Calendar.getInstance()
        
        when (interval) {
            RecurringInterval.LAST_DAY_OF_MONTH -> {
                calendar.timeInMillis = lastDate
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            RecurringInterval.LAST_WEEKDAY_OF_MONTH -> {
                calendar.timeInMillis = lastDate
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, lastDay)
                while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
                      calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                }
            }
            else -> {
                calendar.timeInMillis = lastDate
                if (interval.days > 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, interval.days)
                }
            }
        }
        return calendar.timeInMillis
    }

    private fun calculateMonthlyAmount(item: RecurringItem): Double {
        return when (item.interval) {
            RecurringInterval.DAILY -> item.amount * 30
            RecurringInterval.WEEKLY -> item.amount * 4.33
            RecurringInterval.BI_WEEKLY -> item.amount * 2.17
            RecurringInterval.MONTHLY -> item.amount
            RecurringInterval.QUARTERLY -> item.amount / 3
            RecurringInterval.YEARLY -> item.amount / 12
            RecurringInterval.LAST_DAY_OF_MONTH, RecurringInterval.LAST_WEEKDAY_OF_MONTH -> item.amount
            else -> item.amount
        }
    }

    private fun analyzePatterns(incomeList: List<Income>, expenseList: List<Expense>): List<RecurringSuggestion> {
        val suggestions = mutableListOf<RecurringSuggestion>()
        val now = System.currentTimeMillis()
        val twentyDaysMs = 20L * 24 * 60 * 60 * 1000
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        val ninetyDaysMs = 90L * 24 * 60 * 60 * 1000
        
        // Analyze expenses by category and amount
        val expenseByKey = expenseList.groupBy { "${it.category}_${String.format("%.0f", it.amount)}" }
        expenseByKey.forEach { (key, expenses) ->
            if (expenses.size >= 2) {
                val sortedByDate = expenses.sortedBy { it.date }
                var totalDays = 0L
                var consistent = true
                
                for (i in 1 until sortedByDate.size) {
                    val daysBetween = sortedByDate[i].date - sortedByDate[i - 1].date
                    totalDays += daysBetween
                    
                    // Check if roughly monthly (25-35 days)
                    if (daysBetween < twentyDaysMs || daysBetween > thirtyDaysMs * 1.2) {
                        consistent = false
                        break
                    }
                }
                
                if (consistent && totalDays > 0) {
                    val avgDays = (totalDays / (sortedByDate.size - 1) / (24 * 60 * 60 * 1000)).toInt()
                    val confidence = sortedByDate.size.toFloat() / 3 // More occurrences = higher confidence
                    
                    suggestions.add(
                        RecurringSuggestion(
                            name = expenses.first().category.displayName,
                            amount = expenses.map { it.amount }.average(),
                            type = "Expense",
                            category = expenses.first().category.displayName,
                            confidence = confidence.coerceAtMost(1f),
                            occurrences = sortedByDate.size,
                            avgDaysBetween = avgDays
                        )
                    )
                }
            }
        }
        
        return suggestions.sortedByDescending { it.confidence }
    }

    private fun getUpcomingTransactions(items: List<RecurringItem>): List<UpcomingTransaction> {
        val upcomingMap = mutableMapOf<Long, MutableList<RecurringItem>>()
        val now = System.currentTimeMillis()
        
        // Get next 60 days of transactions
        items.filter { it.status == RecurringStatus.ACTIVE && it.nextDate < now + (60L * 24 * 60 * 60 * 1000) }.forEach { item ->
            var processDate = item.nextDate
            repeat(3) { // Add up to 3 occurrences
                if (processDate > now) {
                    val dayKey = processDate / (24 * 60 * 60 * 1000)
                    upcomingMap.getOrPut(dayKey) { mutableListOf() }.add(item)
                }
                processDate = calculateNextDate(processDate, item.interval)
            }
        }
        
        return upcomingMap.map { (dayKey, dayItems) ->
            UpcomingTransaction(
                date = dayKey * 24 * 60 * 60 * 1000,
                items = dayItems
            )
        }.sortedBy { it.date }
    }

    private fun generateWarnings(upcoming: List<UpcomingTransaction>): List<String> {
        val warnings = mutableListOf<String>()
        
        // Check for date conflicts (many bills on same day/week)
        val weekBuckets = upcoming.groupBy { it.date / (7 * 24 * 60 * 60 * 1000) }
        weekBuckets.forEach { (_, weekTransactions) ->
            val totalAmount = weekTransactions.sumOf { tx -> tx.items.sumOf { it.amount } }
            if (totalAmount > 10000) {
                warnings.add("High expenses coming: ₹${String.format("%.0f", totalAmount)} this week")
            }
        }
        
        return warnings
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun toggleBulkMode() {
        _uiState.update { it.copy(isBulkMode = !it.isBulkMode, selectedItems = emptySet()) }
    }

    fun toggleItemSelection(id: Long) {
        _uiState.update { state ->
            val newSelected = if (state.selectedItems.contains(id)) {
                state.selectedItems - id
            } else {
                state.selectedItems + id
            }
            state.copy(selectedItems = newSelected)
        }
    }

    fun pauseItem(item: RecurringItem, months: Int) {
        viewModelScope.launch {
            val pauseUntil = Calendar.getInstance().apply {
                add(Calendar.MONTH, months)
            }.timeInMillis
            
            // Update in repository (would need to add pause fields to entity)
            loadRecurringItems()
        }
    }

    fun skipNextOccurrence(item: RecurringItem) {
        viewModelScope.launch {
            val nextDate = calculateNextDate(item.nextDate, item.interval)
            // Update in repository
            loadRecurringItems()
        }
    }

    fun markForApproval(item: RecurringItem) {
        viewModelScope.launch {
            // Update status to NEEDS_APPROVAL
            loadRecurringItems()
        }
    }

    fun approveItem(item: RecurringItem) {
        viewModelScope.launch {
            // Update status to ACTIVE and add the transaction
            if (item.type == "Income") {
                incomeRepository.insertIncome(
                    Income(
                        source = item.name,
                        amount = item.amount,
                        category = IncomeCategory.OTHERS,
                        date = System.currentTimeMillis(),
                        isRecurring = true,
                        recurringInterval = item.interval,
                        notes = item.notes
                    )
                )
            }
            loadRecurringItems()
        }
    }

    fun saveRecurringIncome(name: String, amount: Double, category: IncomeCategory, interval: RecurringInterval, notes: String?, endCondition: EndCondition = EndCondition.NEVER, endDate: Long? = null, occurrencesRemaining: Int? = null) {
        viewModelScope.launch {
            val income = Income(
                source = name,
                amount = amount,
                category = category,
                date = System.currentTimeMillis(),
                isRecurring = true,
                recurringInterval = interval,
                notes = notes
            )
            incomeRepository.insertIncome(income)
            hideAddDialog()
            loadRecurringItems()
        }
    }

    fun saveRecurringExpense(name: String, amount: Double, category: ExpenseCategory, interval: RecurringInterval, notes: String?, endCondition: EndCondition = EndCondition.NEVER, endDate: Long? = null, occurrencesRemaining: Int? = null) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                category = category,
                date = System.currentTimeMillis(),
                notes = notes,
                isRecurring = true,
                recurringInterval = interval,
                status = RecurringStatus.ACTIVE,
                endCondition = endCondition,
                endDate = endDate,
                occurrencesRemaining = occurrencesRemaining
            )
            expenseRepository.insertExpense(expense)
            hideAddDialog()
            loadRecurringItems()
        }
    }

    fun deleteRecurring(item: RecurringItem) {
        viewModelScope.launch {
            if (item.type == "Income") {
                incomeRepository.deleteIncome(item.id)
            } else if (item.type == "Expense") {
                expenseRepository.deleteExpense(item.id)
            }
            loadRecurringItems()
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedItems
            // Delete all selected items
            loadRecurringItems()
            _uiState.update { it.copy(selectedItems = emptySet(), isBulkMode = false) }
        }
    }

    fun pauseSelected(months: Int) {
        viewModelScope.launch {
            // Pause all selected items
            loadRecurringItems()
            _uiState.update { it.copy(selectedItems = emptySet(), isBulkMode = false) }
        }
    }

    fun refresh() {
        loadRecurringItems()
    }
}