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
    val isLoading: Boolean = false
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

            combine(
                incomeRepository.getTotalIncomeByDateRange(startOfMonth, endOfMonth),
                expenseRepository.getTotalExpensesByDateRange(startOfMonth, endOfMonth),
                expenseRepository.getExpensesByCategoryGrouped(startOfMonth, endOfMonth)
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

                Triple(
                    Triple(income, expenses, savings),
                    savingsRate,
                    categoryBreakdown
                )
            }.collect { (totals, rate, breakdown) ->
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

        loadMonthlyTrend()
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

    fun selectMonth(month: Int, year: Int) {
        _uiState.update { it.copy(selectedMonth = month, selectedYear = year) }
        loadReportsData()
    }

    fun refreshData() {
        loadReportsData()
    }
}