package com.rudra.savingbuddy.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class DayTransactionSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val hasIncome: Boolean = false,
    val hasExpense: Boolean = false,
    val transactionCount: Int = 0
)

data class CalendarUiState(
    val expenses: List<Expense> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlyNet: Double = 0.0,
    val currentMonth: YearMonth = YearMonth.now(),
    val daySummaries: Map<LocalDate, DayTransactionSummary> = emptyMap(),
    val isLoading: Boolean = false,
    val filterType: FilterType = FilterType.ALL
)

enum class FilterType {
    ALL, INCOME, EXPENSE
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun setFilter(filterType: FilterType) {
        _uiState.value = _uiState.value.copy(filterType = filterType)
    }

    fun setMonth(month: YearMonth) {
        _uiState.value = _uiState.value.copy(currentMonth = month)
        loadTransactionsForMonth(month)
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val expenses = expenseRepository.getAllExpenses().first()
                val incomes = incomeRepository.getAllIncome().first()
                
                val daySummaries = buildDaySummaries(expenses, incomes)
                
                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    incomes = incomes,
                    totalIncome = incomes.sumOf { it.amount },
                    totalExpense = expenses.sumOf { it.amount },
                    daySummaries = daySummaries,
                    monthlyIncome = incomes.sumOf { it.amount },
                    monthlyExpense = expenses.sumOf { it.amount },
                    monthlyNet = incomes.sumOf { it.amount } - expenses.sumOf { it.amount },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun loadTransactionsForMonth(month: YearMonth) {
        viewModelScope.launch {
            try {
                val startOfMonth = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = month.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                val expenses = expenseRepository.getExpensesByDateRange(startOfMonth, endOfMonth).first()
                val incomes = incomeRepository.getIncomeByDateRange(startOfMonth, endOfMonth).first()
                
                val daySummaries = buildDaySummaries(expenses, incomes)
                
                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    incomes = incomes,
                    daySummaries = daySummaries,
                    monthlyIncome = incomes.sumOf { it.amount },
                    monthlyExpense = expenses.sumOf { it.amount },
                    monthlyNet = incomes.sumOf { it.amount } - expenses.sumOf { it.amount }
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    private fun buildDaySummaries(expenses: List<Expense>, incomes: List<Income>): Map<LocalDate, DayTransactionSummary> {
        val summaries = mutableMapOf<LocalDate, DayTransactionSummary>()
        
        expenses.forEach { expense ->
            val date = java.time.Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            
            val current = summaries[date] ?: DayTransactionSummary()
            summaries[date] = current.copy(
                totalExpense = current.totalExpense + expense.amount,
                hasExpense = true,
                transactionCount = current.transactionCount + 1
            )
        }
        
        incomes.forEach { income ->
            val date = java.time.Instant.ofEpochMilli(income.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            
            val current = summaries[date] ?: DayTransactionSummary()
            summaries[date] = current.copy(
                totalIncome = current.totalIncome + income.amount,
                hasIncome = true,
                transactionCount = current.transactionCount + 1
            )
        }
        
        return summaries
    }

    fun getTransactionsForDate(date: LocalDate): List<Any> {
        val state = _uiState.value
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val filteredExpenses = when (state.filterType) {
            FilterType.ALL -> state.expenses
            FilterType.EXPENSE -> state.expenses
            FilterType.INCOME -> emptyList()
        }
        
        val filteredIncomes = when (state.filterType) {
            FilterType.ALL -> state.incomes
            FilterType.INCOME -> state.incomes
            FilterType.EXPENSE -> emptyList()
        }

        val expensesOnDate: List<Any> = filteredExpenses.filter { it.date in startOfDay until endOfDay }
        val incomesOnDate: List<Any> = filteredIncomes.filter { it.date in startOfDay until endOfDay }

        val combined = expensesOnDate + incomesOnDate
        return if (combined.isNotEmpty()) {
            combined.sortedByDescending { obj ->
                when (obj) {
                    is Expense -> obj.date
                    is Income -> obj.date
                    else -> 0L
                }
            }
        } else emptyList()
    }

    fun getDaySummary(date: LocalDate): DayTransactionSummary {
        return _uiState.value.daySummaries[date] ?: DayTransactionSummary()
    }
}