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
import java.time.ZoneId
import javax.inject.Inject

data class CalendarUiState(
    val expenses: List<Expense> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = false
) {
    fun getTransactionsForDate(date: LocalDate): List<Any> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val expensesOnDate = expenses.filter { it.date in startOfDay until endOfDay }
        val incomesOnDate = incomes.filter { it.date in startOfDay until endOfDay }

        return (expensesOnDate + incomesOnDate).sortedByDescending {
            when (it) {
                is Expense -> it.date
                is Income -> it.date
                else -> 0L
            }
        }
    }
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

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val expenses = expenseRepository.getAllExpenses().first()
                val incomes = incomeRepository.getAllIncome().first()
                
                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    incomes = incomes,
                    totalIncome = incomes.sumOf { it.amount },
                    totalExpense = expenses.sumOf { it.amount },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}