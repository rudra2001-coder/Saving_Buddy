package com.rudra.savingbuddy.ui.screens.dashboard

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.data.local.dao.CategoryTotal
import com.rudra.savingbuddy.domain.model.AccountHealth
import com.rudra.savingbuddy.domain.model.BillReminder
import com.rudra.savingbuddy.domain.model.Goal
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

// ─── UI state ─────────────────────────────────────────────────────────────────

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
    val availableAccounts: List<AccountSelection> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class TransactionItem(
    val id: Long,
    val type: String,       // "INCOME" | "EXPENSE"
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

// ─── ViewModel ────────────────────────────────────────────────────────────────

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

    // ── Accounts ──────────────────────────────────────────────────────────────

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAllAccounts().collect { accounts ->
                if (accounts.isEmpty()) {
                    _uiState.update { it.copy(
                        availableAccounts = emptyList(),
                        selectedAccountId = null,
                        selectedAccountName = "Wallet",
                        selectedAccountBalance = 0.0,
                        mainBalance = 0.0
                    )}
                    return@collect
                }

                val list = accounts.map { acc ->
                    AccountSelection(
                        id = acc.id,
                        name = acc.name,
                        balance = acc.balance,
                        iconColor = acc.iconColor
                    )
                }

                val savedId = prefs.getLong("dashboard_selected_account_id", -1L)
                val selected = when {
                    savedId > 0 -> accounts.find { it.id == savedId }
                    else        -> accounts.find { it.name == "Wallet" } ?: accounts.first()
                }

                _uiState.update { it.copy(
                    availableAccounts = list,
                    selectedAccountId = selected?.id,
                    selectedAccountName = selected?.name ?: list.first().name,
                    selectedAccountBalance = selected?.balance ?: 0.0,
                    mainBalance = selected?.balance ?: list.first().balance
                )}
            }
        }
    }

    fun selectAccount(accountId: Long) {
        val account = _uiState.value.availableAccounts.find { it.id == accountId } ?: return
        prefs.edit().putLong("dashboard_selected_account_id", accountId).apply()
        _uiState.update { it.copy(
            selectedAccountId = accountId,
            selectedAccountName = account.name,
            selectedAccountBalance = account.balance,
            mainBalance = account.balance
        )}
    }

    fun clearAccountSelection() {
        prefs.edit().remove("dashboard_selected_account_id").apply()
        loadAccounts()
    }

    // ── Core data ─────────────────────────────────────────────────────────────

    private fun loadDashboardData() {
        viewModelScope.launch { _uiState.update { it.copy(isLoading = true, error = null) } }

        val now          = System.currentTimeMillis()
        val startOfToday = DateUtils.getStartOfDay(now)
        val endOfToday   = DateUtils.getEndOfDay(now)
        val startOfMonth = DateUtils.getStartOfMonth(now)
        val endOfMonth   = DateUtils.getEndOfMonth(now)

        viewModelScope.launch {
            combine(
                incomeRepository.getTotalIncomeByDateRange(startOfToday, endOfToday),
                expenseRepository.getTotalExpensesByDateRange(startOfToday, endOfToday),
                incomeRepository.getTotalIncomeByDateRange(startOfMonth, endOfMonth),
                expenseRepository.getTotalExpensesByDateRange(startOfMonth, endOfMonth),
                expenseRepository.getExpensesByCategoryGrouped(startOfMonth, endOfMonth),
                budgetRepository.getBudget()
            ) { values ->
                val todayIncome      = (values[0] as? Double) ?: 0.0
                val todayExpenses    = (values[1] as? Double) ?: 0.0
                val monthlyIncome    = (values[2] as? Double) ?: 0.0
                val monthlyExpenses  = (values[3] as? Double) ?: 0.0
                @Suppress("UNCHECKED_CAST")
                val categories       = values[4] as List<CategoryTotal>
                val budget           = values[5] as? com.rudra.savingbuddy.domain.model.Budget

                val budgetAmount  = budget?.monthlyLimit ?: 0.0
                val budgetWarning = budgetAmount > 0 && monthlyExpenses >= budgetAmount * 0.8

                DashboardUiState(
                    todayIncome     = todayIncome,
                    todayExpenses   = todayExpenses,
                    todaySavings    = todayIncome - todayExpenses,
                    monthlyIncome   = monthlyIncome,
                    monthlyExpenses = monthlyExpenses,
                    monthlySavings  = monthlyIncome - monthlyExpenses,
                    budget          = budgetAmount,
                    budgetWarning   = budgetWarning,
                    expensesByCategory = categories,
                    insights        = generateInsights(monthlyIncome, monthlyExpenses, todayExpenses, startOfMonth)
                )
            }.collect { partial ->
                _uiState.update { current -> current.copy(
                    todayIncome        = partial.todayIncome,
                    todayExpenses      = partial.todayExpenses,
                    todaySavings       = partial.todaySavings,
                    monthlyIncome      = partial.monthlyIncome,
                    monthlyExpenses    = partial.monthlyExpenses,
                    monthlySavings     = partial.monthlySavings,
                    budget             = partial.budget,
                    budgetWarning      = partial.budgetWarning,
                    expensesByCategory = partial.expensesByCategory,
                    insights           = partial.insights,
                    isLoading          = false
                )}
            }
        }

        loadRecentTransactions(startOfMonth, endOfMonth)
        loadGoalsAndBills()
        loadMonthlyTrend()
        loadAccountHealth()
    }

    // ── Account health + net worth ────────────────────────────────────────────

    private fun loadAccountHealth() {
        viewModelScope.launch {
            fusionRepository.getNetWorthSummary().collect { nw ->
                _uiState.update { it.copy(netWorth = nw.netWorth, totalAssets = nw.totalAssets) }
            }
        }
        viewModelScope.launch {
            fusionRepository.getAccountHealthList().collect { list ->
                _uiState.update { it.copy(accountHealthList = list) }
            }
        }
    }

    // ── Goals + bills ─────────────────────────────────────────────────────────

    private fun loadGoalsAndBills() {
        viewModelScope.launch {
            goalRepository.getActiveGoals().collect { goals ->
                _uiState.update { it.copy(activeGoal = goals.firstOrNull { g -> !g.isCompleted }) }
            }
        }
        viewModelScope.launch {
            billReminderRepository.getActiveBillReminders().collect { bills ->
                _uiState.update {
                    it.copy(upcomingBills = bills.filter { b -> b.isActive }.sortedBy { b -> b.billingDay }.take(3))
                }
            }
        }
    }

    // ── Monthly trend ─────────────────────────────────────────────────────────

    private fun loadMonthlyTrend() {
        viewModelScope.launch {
            val now   = System.currentTimeMillis()
            val trend = mutableListOf<Double>()
            for (i in 6 downTo 0) {
                val offset = i * 30L * 24 * 60 * 60 * 1000
                val ms     = DateUtils.getStartOfMonth(now - offset)
                val me     = DateUtils.getEndOfMonth(now - offset)
                val inc = incomeRepository.getTotalIncomeByDateRange(ms, me).first() ?: 0.0
                val exp = expenseRepository.getTotalExpensesByDateRange(ms, me).first() ?: 0.0
                trend.add(inc - exp)
            }
            _uiState.update { it.copy(monthlyTrend = trend) }
        }
    }

    // ── Recent transactions ───────────────────────────────────────────────────

    private fun loadRecentTransactions(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            combine(
                incomeRepository.getIncomeByDateRange(startDate, endDate),
                expenseRepository.getExpensesByDateRange(startDate, endDate)
            ) { incomes, expenses ->
                val list = mutableListOf<TransactionItem>()
                incomes.take(5).forEach { i ->
                    list += TransactionItem(i.id, "INCOME", i.source, i.amount, i.category.displayName, i.date)
                }
                expenses.take(5).forEach { e ->
                    list += TransactionItem(e.id, "EXPENSE", e.category.displayName, e.amount, e.category.displayName, e.date)
                }
                list.sortedByDescending { it.date }.take(10)
            }.collect { txs -> _uiState.update { it.copy(recentTransactions = txs) } }
        }
    }

    // ── Insights ──────────────────────────────────────────────────────────────

    private fun generateInsights(
        monthlyIncome: Double,
        monthlyExpenses: Double,
        todayExpenses: Double,
        @Suppress("UNUSED_PARAMETER") startOfMonth: Long
    ): List<String> = buildList {
        if (monthlyExpenses > monthlyIncome * 0.9)
            add("Warning: You've spent over 90% of your income this month")
        if (todayExpenses > 100)
            add("You spent ${String.format("৳%.2f", todayExpenses)} today")
        if (monthlyExpenses < monthlyIncome * 0.5 && monthlyIncome > 0)
            add("Great job! You're saving over 50% of your income this month")
    }

    // ── Public actions ────────────────────────────────────────────────────────

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                loadDashboardData()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
