package com.rudra.savingbuddy.ui.screens.dashboard

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.data.local.dao.CategoryTotal
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.AccountHealth
import com.rudra.savingbuddy.domain.model.BillReminder
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Goal
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.NetWorthSummary
import com.rudra.savingbuddy.domain.repository.AccountRepository
import com.rudra.savingbuddy.domain.repository.BillReminderRepository
import com.rudra.savingbuddy.domain.repository.BudgetRepository
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.FusionRepository
import com.rudra.savingbuddy.domain.repository.GoalRepository
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
    val budget: Double = 0.0,
    val budgetWarning: Boolean = false,
    val expensesByCategory: List<CategoryTotal> = emptyList(),
    val recentTransactions: List<TransactionItem> = emptyList(),
    val insights: List<String> = emptyList(),
    val activeGoal: Goal? = null,
    val upcomingBills: List<BillReminder> = emptyList(),
    val monthlyTrend: List<Double> = emptyList(),
    val mainBalance: Double = 0.0,
    val netWorth: Double = 0.0,
    val totalAssets: Double = 0.0,
    val accountHealthList: List<AccountHealth> = emptyList(),
    val selectedAccountId: Long? = null,
    val selectedAccountName: String = "Wallet",
    val selectedAccountBalance: Double = 0.0,
    val availableAccounts: List<AccountSelection> = emptyList()
)

data class TransactionItem(
    val id: Long,
    val type: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Long
)

data class AccountSelection(
    val id: Long,
    val name: String,
    val balance: Double,
    val iconColor: Long
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository,
    private val billReminderRepository: BillReminderRepository,
    private val fusionRepository: FusionRepository,
    private val accountRepository: AccountRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAllAccounts().collect { accounts ->
                val accountList = accounts.map { acc ->
                    AccountSelection(
                        id = acc.id,
                        name = acc.name,
                        balance = acc.balance,
                        iconColor = acc.iconColor
                    )
                }
                
                val savedAccountId = prefs.getLong("dashboard_selected_account_id", -1L)
                val selectedAccount = if (savedAccountId > 0) {
                    accounts.find { it.id == savedAccountId }
                } else {
                    accounts.find { it.name == "Wallet" } ?: accounts.firstOrNull()
                }

                _uiState.update { state ->
                    state.copy(
                        availableAccounts = accountList,
                        selectedAccountId = selectedAccount?.id,
                        selectedAccountName = selectedAccount?.name ?: "Wallet",
                        selectedAccountBalance = selectedAccount?.balance ?: state.mainBalance,
                        mainBalance = selectedAccount?.balance ?: state.mainBalance
                    )
                }
            }
        }
    }

    fun selectAccount(accountId: Long) {
        val state = _uiState.value
        val account = state.availableAccounts.find { it.id == accountId }
        
        if (account != null) {
            prefs.edit().putLong("dashboard_selected_account_id", accountId).apply()
            _uiState.update { it.copy(
                selectedAccountId = accountId,
                selectedAccountName = account.name,
                selectedAccountBalance = account.balance,
                mainBalance = account.balance
            )}
        }
    }

    fun clearAccountSelection() {
        prefs.edit().remove("dashboard_selected_account_id").apply()
        loadAccounts()
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
                val budgetAmount = budget?.monthlyLimit ?: 0.0
                val budgetWarning = budgetAmount > 0 && monthlyExpenses >= (budgetAmount * 0.8)

                val insights = generateInsights(monthlyIncome, monthlyExpenses, todayExpenses, startOfMonth)

                DashboardUiState(
                    todayIncome = todayIncome,
                    todayExpenses = todayExpenses,
                    todaySavings = todaySavings,
                    monthlyIncome = monthlyIncome,
                    monthlyExpenses = monthlyExpenses,
                    monthlySavings = monthlySavings,
                    budget = budgetAmount,
                    budgetWarning = budgetWarning,
                    expensesByCategory = expensesByCategory,
                    insights = insights
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        loadRecentTransactions(startOfMonth, endOfMonth)
        loadGoalsAndBills()
        loadMonthlyTrend()
        loadAccountHealth()
    }

    private fun loadAccountHealth() {
        viewModelScope.launch {
            fusionRepository.getNetWorthSummary().collect { netWorth ->
                _uiState.update { 
                    it.copy(
                        netWorth = netWorth.netWorth,
                        totalAssets = netWorth.totalAssets
                    ) 
                }
            }
        }
        
        viewModelScope.launch {
            fusionRepository.getAccountHealthList().collect { healthList ->
                _uiState.update { it.copy(accountHealthList = healthList) }
            }
        }
    }

    private fun loadGoalsAndBills() {
        viewModelScope.launch {
            goalRepository.getActiveGoals().collect { goals ->
                val activeGoal = goals.firstOrNull { !it.isCompleted }
                _uiState.update { it.copy(activeGoal = activeGoal) }
            }
        }

        viewModelScope.launch {
            billReminderRepository.getActiveBillReminders().collect { bills ->
                val upcomingBills = bills
                    .filter { it.isActive }
                    .sortedBy { it.billingDay }
                    .take(3)
                _uiState.update { it.copy(upcomingBills = upcomingBills) }
            }
        }
    }

    private fun loadMonthlyTrend() {
        viewModelScope.launch {
            val today = System.currentTimeMillis()
            val trend = mutableListOf<Double>()
            
            for (i in 6 downTo 0) {
                val monthStart = DateUtils.getStartOfMonth(today - (i * 30L * 24 * 60 * 60 * 1000))
                val monthEnd = DateUtils.getEndOfMonth(today - (i * 30L * 24 * 60 * 60 * 1000))
                
                combine(
                    incomeRepository.getTotalIncomeByDateRange(monthStart, monthEnd),
                    expenseRepository.getTotalExpensesByDateRange(monthStart, monthEnd)
                ) { income, expense ->
                    (income as? Double ?: 0.0) - (expense as? Double ?: 0.0)
                }.first().let { savings ->
                    trend.add(savings)
                }
            }
            
            _uiState.update { it.copy(monthlyTrend = trend) }
        }
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