package com.rudra.savingbuddy.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.data.local.dao.CategoryTotal
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.repository.BudgetRepository
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import com.rudra.savingbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val todayIncome: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todaySavings: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val monthlySavings: Double = 0.0,
    val budget: Double? = null,
    val budgetWarning: Boolean = false,
    val expensesByCategory: List<CategoryTotal> = emptyList(),
    val recentTransactions: List<TransactionItem> = emptyList(),
    val insights: List<String> = emptyList()
)

data class TransactionItem(
    val id: Long,
    val type: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Long
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val today = System.currentTimeMillis()
        val startOfToday = DateUtils.getStartOfDay(today)
        val endOfToday = DateUtils.getEndOfDay(today)
        val startOfMonth = DateUtils.getStartOfMonth(today)
        val endOfMonth = DateUtils.getEndOfMonth(today)

        viewModelScope.launch {
            combine(
                incomeRepository.getTotalIncomeByDateRange(startOfToday, endOfToday),
                expenseRepository.getTotalExpensesByDateRange(startOfToday, endOfToday),
                incomeRepository.getTotalIncomeByDateRange(startOfMonth, endOfMonth),
                expenseRepository.getTotalExpensesByDateRange(startOfMonth, endOfMonth),
                expenseRepository.getExpensesByCategoryGrouped(startOfMonth, endOfMonth),
                budgetRepository.getBudget()
            ) { values ->
                val todayIncome = (values[0] as? Double) ?: 0.0
                val todayExpenses = (values[1] as? Double) ?: 0.0
                val monthlyIncome = (values[2] as? Double) ?: 0.0
                val monthlyExpenses = (values[3] as? Double) ?: 0.0
                @Suppress("UNCHECKED_CAST")
                val expensesByCategory = values[4] as List<CategoryTotal>
                val budget = values[5] as? com.rudra.savingbuddy.domain.model.Budget

                val todaySavings = todayIncome - todayExpenses
                val monthlySavings = monthlyIncome - monthlyExpenses
                val budgetWarning = budget != null && monthlyExpenses >= (budget.monthlyLimit * 0.8)

                val insights = generateInsights(monthlyIncome, monthlyExpenses, todayExpenses, startOfMonth)

                DashboardUiState(
                    todayIncome = todayIncome,
                    todayExpenses = todayExpenses,
                    todaySavings = todaySavings,
                    monthlyIncome = monthlyIncome,
                    monthlyExpenses = monthlyExpenses,
                    monthlySavings = monthlySavings,
                    budget = budget?.monthlyLimit,
                    budgetWarning = budgetWarning,
                    expensesByCategory = expensesByCategory,
                    insights = insights
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        loadRecentTransactions(startOfMonth, endOfMonth)
    }

    private fun loadRecentTransactions(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            combine(
                incomeRepository.getIncomeByDateRange(startDate, endDate),
                expenseRepository.getExpensesByDateRange(startDate, endDate)
            ) { incomes, expenses ->
                val transactions = mutableListOf<TransactionItem>()
                
                incomes.take(5).forEach { income ->
                    transactions.add(
                        TransactionItem(
                            id = income.id,
                            type = "INCOME",
                            title = income.source,
                            amount = income.amount,
                            category = income.category.displayName,
                            date = income.date
                        )
                    )
                }
                
                expenses.take(5).forEach { expense ->
                    transactions.add(
                        TransactionItem(
                            id = expense.id,
                            type = "EXPENSE",
                            title = expense.category.displayName,
                            amount = expense.amount,
                            category = expense.category.displayName,
                            date = expense.date
                        )
                    )
                }
                
                transactions.sortedByDescending { it.date }.take(10)
            }.collect { transactions ->
                _uiState.update { it.copy(recentTransactions = transactions) }
            }
        }
    }

    private fun generateInsights(monthlyIncome: Double, monthlyExpenses: Double, todayExpenses: Double, startOfMonth: Long): List<String> {
        val insights = mutableListOf<String>()
        
        if (monthlyExpenses > monthlyIncome * 0.9) {
            insights.add("Warning: You've spent over 90% of your income this month")
        }
        
        if (todayExpenses > 100) {
            insights.add("You spent ${String.format("$%.2f", todayExpenses)} today")
        }
        
        if (monthlyExpenses < monthlyIncome * 0.5) {
            insights.add("Great job! You're saving well this month")
        }
        
        return insights
    }

    fun refreshData() {
        loadDashboardData()
    }
}