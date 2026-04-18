package com.rudra.savingbuddy.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.IncomeCategory
import com.rudra.savingbuddy.domain.model.RecurringInterval
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
    val type: String, // "Income" or "Expense"
    val category: String,
    val interval: RecurringInterval,
    val nextDate: Long,
    val notes: String?
)

data class RecurringUiState(
    val recurringItems: List<RecurringItem> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

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
                val recurringItems = mutableListOf<RecurringItem>()
                
                incomeList.filter { it.isRecurring }.forEach { income ->
                    recurringItems.add(
                        RecurringItem(
                            id = income.id,
                            name = income.source.ifBlank { income.category.displayName },
                            amount = income.amount,
                            type = "Income",
                            category = income.category.displayName,
                            interval = income.recurringInterval ?: RecurringInterval.MONTHLY,
                            nextDate = calculateNextDate(income.date, income.recurringInterval),
                            notes = income.notes
                        )
                    )
                }
                
                expenseList.filter { it.isRecurring }.forEach { expense ->
                    recurringItems.add(
                        RecurringItem(
                            id = expense.id,
                            name = expense.category.displayName,
                            amount = expense.amount,
                            type = "Expense",
                            category = expense.category.displayName,
                            interval = RecurringInterval.MONTHLY,
                            nextDate = calculateNextDate(expense.date, RecurringInterval.MONTHLY),
                            notes = expense.notes
                        )
                    )
                }
                
                recurringItems
            }.collect { items ->
                _uiState.update { 
                    it.copy(
                        recurringItems = items.sortedBy { it.nextDate },
                        isLoading = false
                    ) 
                }
            }
        }
    }

    private fun calculateNextDate(lastDate: Long, interval: RecurringInterval?): Long {
        if (interval == null) return lastDate
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = lastDate
        when (interval) {
            RecurringInterval.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            RecurringInterval.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringInterval.MONTHLY -> calendar.add(Calendar.MONTH, 1)
        }
        return calendar.timeInMillis
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun saveRecurringIncome(
        name: String,
        amount: Double,
        category: IncomeCategory,
        interval: RecurringInterval,
        notes: String?
    ) {
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

    fun saveRecurringExpense(
        name: String,
        amount: Double,
        category: ExpenseCategory,
        interval: RecurringInterval,
        notes: String?
    ) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                category = category,
                date = System.currentTimeMillis(),
                notes = notes
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

    fun refresh() {
        loadRecurringItems()
    }
}