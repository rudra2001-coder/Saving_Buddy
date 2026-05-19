package com.rudra.savingbuddy.ui.screens.reports

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
import com.rudra.savingbuddy.util.ReportConfig
import com.rudra.savingbuddy.util.ReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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
    val isThisYear: Boolean = false,
    val filteredIncomes: List<Income> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val insights: List<ReportInsight> = emptyList(),
    val pdfGenerationState: PdfState = PdfState.Idle,
    val comparisonData: ComparisonData? = null
)

data class ReportInsight(
    val title: String,
    val description: String,
    val type: InsightType = InsightType.INFO
)

enum class InsightType { SUCCESS, WARNING, ERROR, INFO }

sealed class PdfState {
    data object Idle : PdfState()
    data object Generating : PdfState()
    data object Success : PdfState()
    data class Error(val message: String) : PdfState()
}

data class ComparisonData(
    val previousIncome: Double,
    val previousExpenses: Double,
    val incomeChange: Double,
    val expenseChange: Double
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
    private val expenseRepository: ExpenseRepository,
    @ApplicationContext private val context: Context
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
            generateInsights()
        }
    }

    private suspend fun generateInsights() {
        val state = _uiState.value
        val insights = mutableListOf<ReportInsight>()
        
        if (state.savingsRate >= 50) {
            insights.add(ReportInsight("Excellent Saver", "You're saving ${state.savingsRate.toInt()}% of income! Outstanding!", InsightType.SUCCESS))
        } else if (state.savingsRate >= 20) {
            insights.add(ReportInsight("Good Progress", "Saving ${state.savingsRate.toInt()}%. Keep going!", InsightType.SUCCESS))
        } else if (state.savingsRate > 0) {
            insights.add(ReportInsight("Room to Improve", "Aim for 20%+ savings rate", InsightType.WARNING))
        } else {
            insights.add(ReportInsight("Overspending Alert", "Expenses exceed income by ${CurrencyFormatter.format(-state.totalSavings)}", InsightType.ERROR))
        }

        val topCategory = state.categoryBreakdown.maxByOrNull { it.percentage }
        if (topCategory != null && topCategory.percentage > 30) {
            insights.add(ReportInsight("High Category Spend", "\"${topCategory.category}\" is ${topCategory.percentage.toInt()}% of expenses", InsightType.WARNING))
        }

        if (state.totalExpenses > state.totalIncome * 0.8 && state.totalIncome > 0) {
            insights.add(ReportInsight("Budget Warning", "${(state.totalExpenses / state.totalIncome * 100).toInt()}% of income spent", InsightType.ERROR))
        }

        if (state.totalIncome > 0 && state.totalExpenses > 0) {
            val ratio = state.totalIncome / state.totalExpenses
            when {
                ratio > 2.0 -> insights.add(ReportInsight("Strong Financials", "Income is ${String.format("%.1f", ratio)}x expenses", InsightType.SUCCESS))
                ratio > 1.5 -> insights.add(ReportInsight("Healthy Ratio", "Income is ${String.format("%.1f", ratio)}x expenses", InsightType.SUCCESS))
                ratio > 1.0 -> insights.add(ReportInsight("Positive Cash Flow", "Income exceeds expenses", InsightType.INFO))
                else -> insights.add(ReportInsight("Negative Cash Flow", "Expenses exceed income by ${String.format("%.1f", 1/ratio)}x", InsightType.ERROR))
            }
        }

        _uiState.update { it.copy(insights = insights) }
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
                val filteredIncomes = mutableListOf<Income>()
                val filteredExpenses = mutableListOf<Expense>()
                
                val incomeFiltered = if (startDate != null) incomeList.filter { it.date >= startDate } else incomeList
                val incomeFinal = if (endDate != null) incomeFiltered.filter { it.date <= endDate } else incomeFiltered
                incomeFinal.forEach { income ->
                    filteredIncomes.add(income)
                    allTransactions.add(TransactionLog(id = income.id, type = "Income", description = income.source, amount = income.amount, category = income.category.displayName, date = income.date, notes = income.notes))
                }
                
                val expenseFiltered = if (startDate != null) expenseList.filter { it.date >= startDate } else expenseList
                val expenseFinal = if (endDate != null) expenseFiltered.filter { it.date <= endDate } else expenseFiltered
                expenseFinal.forEach { expense ->
                    filteredExpenses.add(expense)
                    allTransactions.add(TransactionLog(id = expense.id, type = "Expense", description = expense.category.displayName, amount = expense.amount, category = expense.category.displayName, date = expense.date, notes = expense.notes))
                }
                
                Triple(allTransactions.sortedByDescending { it.date }, filteredIncomes, filteredExpenses)
            }.first().let { (logs, incomes, expenses) ->
                var filtered = logs
                
                if (type != null) filtered = filtered.filter { it.type == type }
                
                _uiState.update { 
                    it.copy(
                        transactionLogs = filtered.take(500),
                        logCount = filtered.size,
                        filteredIncomes = incomes,
                        filteredExpenses = expenses,
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

    fun generatePdfReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(pdfGenerationState = PdfState.Generating) }
            try {
                val state = _uiState.value
                val startDate = state.overviewStartDate ?: DateUtils.getStartOfMonth()
                val endDate = state.overviewEndDate ?: DateUtils.getEndOfMonth()

                val incomes = incomeRepository.getIncomeByDateRange(startDate, endDate).first()
                val expenses = expenseRepository.getExpensesByDateRange(startDate, endDate).first()

                val config = ReportConfig(
                    title = "Monthly Financial Report",
                    startDate = startDate,
                    endDate = endDate
                )
                val reportData = ReportGenerator.generateReportData(
                    incomes = incomes,
                    expenses = expenses,
                    config = config
                )
                val intent = ReportGenerator.generatePdfReport(context, reportData)
                if (intent != null) {
                    context.startActivity(Intent.createChooser(intent, "Share Report"))
                    _uiState.update { it.copy(pdfGenerationState = PdfState.Success) }
                    delay(1500)
                    _uiState.update { it.copy(pdfGenerationState = PdfState.Idle) }
                } else {
                    _uiState.update { it.copy(pdfGenerationState = PdfState.Error("Failed to generate PDF")) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(pdfGenerationState = PdfState.Error(e.message ?: "Unknown error")) }
            }
        }
    }
}