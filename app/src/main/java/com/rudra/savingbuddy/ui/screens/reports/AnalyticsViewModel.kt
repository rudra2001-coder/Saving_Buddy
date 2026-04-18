package com.rudra.savingbuddy.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import com.rudra.savingbuddy.domain.repository.SettingsRepository
import com.rudra.savingbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AnalyticsUiState(
    val customStartDate: Long = DateUtils.getStartOfMonth(),
    val customEndDate: Long = DateUtils.getEndOfMonth(),
    val selectedRange: DateRange = DateRange.THIS_MONTH,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val savings: Double = 0.0,
    val savingsRate: Double = 0.0,
    val expensesByCategory: List<AnalyticsCategoryBreakdown> = emptyList(),
    val dailySpending: List<DailySpending> = emptyList(),
    val yearOverYear: List<YearComparison> = emptyList(),
    val isLoading: Boolean = false
)

enum class DateRange(val displayName: String, val getDates: () -> Pair<Long, Long>) {
    TODAY("Today", { 
        val now = System.currentTimeMillis()
        Pair(DateUtils.getStartOfDay(now), DateUtils.getEndOfDay(now))
    }),
    THIS_WEEK("This Week", {
        val now = System.currentTimeMillis()
        Pair(DateUtils.getStartOfWeek(now), DateUtils.getEndOfDay(now))
    }),
    LAST_WEEK("Last Week", {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        Pair(DateUtils.getStartOfWeek(calendar.timeInMillis), DateUtils.getEndOfWeek(calendar.timeInMillis))
    }),
    THIS_MONTH("This Month", {
        val now = System.currentTimeMillis()
        Pair(DateUtils.getStartOfMonth(now), DateUtils.getEndOfMonth(now))
    }),
    LAST_MONTH("Last Month", {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }
        Pair(DateUtils.getStartOfMonth(calendar.timeInMillis), DateUtils.getEndOfMonth(calendar.timeInMillis))
    }),
    LAST_3_MONTHS("Last 3 Months", {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, -3)
        }
        Pair(DateUtils.getStartOfMonth(calendar.timeInMillis), DateUtils.getEndOfMonth())
    }),
    THIS_YEAR("This Year", {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        Pair(calendar.timeInMillis, System.currentTimeMillis())
    }),
    CUSTOM("Custom", { 
        Pair(DateUtils.getStartOfMonth(), DateUtils.getEndOfMonth())
    })
}

data class AnalyticsCategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val change: Double = 0.0
)

data class DailySpending(
    val date: Long,
    val amount: Double
)

data class YearComparison(
    val month: String,
    val currentYear: Double,
    val previousYear: Double,
    val change: Double
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    fun selectDateRange(range: DateRange) {
        val (start, end) = range.getDates()
        _uiState.update { it.copy(selectedRange = range, customStartDate = start, customEndDate = end) }
        loadAnalytics()
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _uiState.update { it.copy(
            customStartDate = start,
            customEndDate = end,
            selectedRange = DateRange.CUSTOM
        ) }
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val start = _uiState.value.customStartDate
            val end = _uiState.value.customEndDate

            combine(
                incomeRepository.getTotalIncomeByDateRange(start, end),
                expenseRepository.getTotalExpensesByDateRange(start, end),
                expenseRepository.getExpensesByCategoryGrouped(start, end)
            ) { income, expenses, categories ->
                Triple(income ?: 0.0, expenses ?: 0.0, categories)
            }.collect { (income, expenses, categories) ->
                val savings = income - expenses
                val savingsRate = if (income > 0) (savings / income) * 100 else 0.0

                val breakdown = categories.map { cat ->
                    AnalyticsCategoryBreakdown(
                        category = cat.category,
                        amount = cat.total,
                        percentage = if (expenses > 0) (cat.total / expenses) * 100 else 0.0
                    )
                }.sortedByDescending { it.amount }

                _uiState.update { it.copy(
                    totalIncome = income,
                    totalExpenses = expenses,
                    savings = savings,
                    savingsRate = savingsRate,
                    expensesByCategory = breakdown,
                    isLoading = false
                ) }
            }
        }

        loadDailySpending()
        loadYearOverYear()
    }

    private fun loadDailySpending() {
        viewModelScope.launch {
            val start = _uiState.value.customStartDate
            val end = _uiState.value.customEndDate

            expenseRepository.getExpensesByDateRange(start, end).collect { expenses ->
                val grouped = expenses.groupBy { 
                    DateUtils.getStartOfDay(it.date)
                }.map { (date, list) ->
                    DailySpending(date, list.sumOf { it.amount })
                }.sortedBy { it.date }

                _uiState.update { it.copy(dailySpending = grouped) }
            }
        }
    }

    private fun loadYearOverYear() {
        viewModelScope.launch {
            val comparisons = mutableListOf<YearComparison>()
            val calendar = Calendar.getInstance()

            for (i in 0 until 12) {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.MONTH, -i)
                
                val monthName = DateUtils.formatMonthYear(calendar.timeInMillis).split(" ").first()
                val start = DateUtils.getStartOfMonth(calendar.timeInMillis)
                val end = DateUtils.getEndOfMonth(calendar.timeInMillis)

                // Current year
                combine(
                    incomeRepository.getTotalIncomeByDateRange(start, end),
                    expenseRepository.getTotalExpensesByDateRange(start, end)
                ) { income, expenses ->
                    (income ?: 0.0) - (expenses ?: 0.0)
                }.first().let { savings ->
                    comparisons.add(YearComparison(monthName, savings, 0.0, 0.0))
                }
            }

            _uiState.update { it.copy(yearOverYear = comparisons) }
        }
    }
}