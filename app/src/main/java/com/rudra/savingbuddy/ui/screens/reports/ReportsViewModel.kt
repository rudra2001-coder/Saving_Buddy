package com.rudra.savingbuddy.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import com.rudra.savingbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ReportsUiState(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSavings: Double = 0.0,
    val savingsRate: Double = 0.0,
    val monthlyData: List<MonthlyData> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdownItem> = emptyList(),
    val selectedMonth: Int = DateUtils.getCurrentMonth(),
    val selectedYear: Int = DateUtils.getCurrentYear(),
    val isLoading: Boolean = false,
    val transactionLogs: List<TransactionLog> = emptyList(),
    val logCount: Int = 0,
    val currentLogPage: Int = 0,
    val filterStartDate: Long? = null,
    val filterEndDate: Long? = null,
    val filterType: String? = null,
    val overviewStartDate: Long? = null,
    val overviewEndDate: Long? = null,
    val isToday: Boolean = false,
    val isThisWeek: Boolean = false,
    val isThisMonth: Boolean = true,
    val isThisYear: Boolean = false
)

data class TransactionLog(
    val id: Long,
    val type: String,
    val description: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val notes: String?
)

data class MonthlyData(
    val month: String,
    val income: Double,
    val expenses: Double,
    val savings: Double
)

data class CategoryBreakdownItem(
    val category: String,
    val amount: Double,
    val percentage: Double
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReportsData()
    }

    private fun loadReportsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val now = System.currentTimeMillis()
            val startOfMonth = DateUtils.getStartOfMonth(now)
            val endOfMonth = DateUtils.getEndOfMonth(now)

            loadDataForRange(startOfMonth, endOfMonth)
            loadMonthlyTrend()
        }
    }

    private suspend fun loadDataForRange(startDate: Long, endDate: Long) {
        combine(
            incomeRepository.getTotalIncomeByDateRange(startDate, endDate),
            expenseRepository.getTotalExpensesByDateRange(startDate, endDate),
            expenseRepository.getExpensesByCategoryGrouped(startDate, endDate)
        ) { totalIncome, totalExpenses, categoryData ->
            val income = totalIncome ?: 0.0
            val expenses = totalExpenses ?: 0.0
            val savings = income - expenses
            val savingsRate = if (income > 0) (savings / income) * 100 else 0.0

            val categoryBreakdown = categoryData.map { cat ->
                CategoryBreakdownItem(
                    category = cat.category,
                    amount = cat.total,
                    percentage = if (expenses > 0) (cat.total / expenses) * 100 else 0.0
                )
            }

            Triple(Triple(income, expenses, savings), savingsRate, categoryBreakdown)
        }.first().let { (totals, rate, breakdown) ->
            _uiState.update {
                it.copy(
                    totalIncome = totals.first,
                    totalExpenses = totals.second,
                    totalSavings = totals.third,
                    savingsRate = rate,
                    categoryBreakdown = breakdown,
                    isLoading = false
                )
            }
        }
    }

    private fun loadMonthlyTrend() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val monthlyData = mutableListOf<MonthlyData>()

            for (i in 5 downTo 0) {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.MONTH, -i)
                
                val startOfMonth = DateUtils.getStartOfMonth(calendar.timeInMillis)
                val endOfMonth = DateUtils.getEndOfMonth(calendar.timeInMillis)
                val monthName = DateUtils.formatMonthYear(calendar.timeInMillis)

                combine(
                    incomeRepository.getTotalIncomeByDateRange(startOfMonth, endOfMonth),
                    expenseRepository.getTotalExpensesByDateRange(startOfMonth, endOfMonth)
                ) { income, expenses ->
                    val inc = income ?: 0.0
                    val exp = expenses ?: 0.0
                    MonthlyData(monthName, inc, exp, inc - exp)
                }.first().let { data ->
                    monthlyData.add(data)
                }
            }

            _uiState.update { it.copy(monthlyData = monthlyData) }
        }
    }

    fun selectDatePreset(preset: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val (startDate, endDate) = when (preset) {
                "today" -> Pair(now - (24 * 60 * 60 * 1000), now)
                "week" -> Pair(now - (7 * 24 * 60 * 60 * 1000), now)
                "month" -> {
                    val startOfMonth = DateUtils.getStartOfMonth(now)
                    Pair(startOfMonth, now)
                }
                "year" -> {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    Pair(calendar.timeInMillis, now)
                }
                else -> Pair(DateUtils.getStartOfMonth(now), now)
            }

            _uiState.update {
                it.copy(
                    overviewStartDate = startDate,
                    overviewEndDate = endDate,
                    isToday = preset == "today",
                    isThisWeek = preset == "week",
                    isThisMonth = preset == "month",
                    isThisYear = preset == "year"
                )
            }

            loadDataForRange(startDate, endDate)
        }
    }

    fun applyDateRangeToOverview(startDate: Long?, endDate: Long?) {
        if (startDate != null && endDate != null) {
            _uiState.update {
                it.copy(
                    overviewStartDate = startDate,
                    overviewEndDate = endDate,
                    isToday = false,
                    isThisWeek = false,
                    isThisMonth = false,
                    isThisYear = false
                )
            }
            viewModelScope.launch {
                loadDataForRange(startDate, endDate)
            }
        }
    }

    fun clearOverviewDateRange() {
        _uiState.update {
            it.copy(
                overviewStartDate = null,
                overviewEndDate = null,
                isToday = false,
                isThisWeek = false,
                isThisMonth = true,
                isThisYear = false
            )
        }
        loadReportsData()
    }

    fun selectMonth(month: Int, year: Int) {
        _uiState.update { it.copy(selectedMonth = month, selectedYear = year) }
        loadReportsData()
    }

    fun refreshData() {
        loadReportsData()
    }

    fun loadTransactionLogs(startDate: Long? = null, endDate: Long? = null, type: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, filterStartDate = startDate, filterEndDate = endDate, filterType = type) }
            
            combine(
                incomeRepository.getIncomePaginated(1000, 0),
                expenseRepository.getExpensesPaginated(1000, 0)
            ) { incomeList, expenseList ->
                val allTransactions = mutableListOf<TransactionLog>()
                
                incomeList.forEach { income ->
                    allTransactions.add(TransactionLog(id = income.id, type = "Income", description = income.source, amount = income.amount, category = income.category.displayName, date = income.date, notes = income.notes))
                }
                
                expenseList.forEach { expense ->
                    allTransactions.add(TransactionLog(id = expense.id, type = "Expense", description = expense.category.displayName, amount = expense.amount, category = expense.category.displayName, date = expense.date, notes = expense.notes))
                }
                
                allTransactions.sortedByDescending { it.date }
            }.first().let { logs ->
                var filtered = logs
                
                if (startDate != null) filtered = filtered.filter { it.date >= startDate }
                if (endDate != null) filtered = filtered.filter { it.date <= endDate }
                if (type != null) filtered = filtered.filter { it.type == type }
                
                _uiState.update { 
                    it.copy(
                        transactionLogs = filtered.take(500),
                        logCount = filtered.size,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun clearFilters() {
        _uiState.update { it.copy(filterStartDate = null, filterEndDate = null, filterType = null) }
        loadTransactionLogs()
    }
}