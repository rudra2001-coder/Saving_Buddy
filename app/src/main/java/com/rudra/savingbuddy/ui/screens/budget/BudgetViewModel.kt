package com.rudra.savingbuddy.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Budget
import com.rudra.savingbuddy.domain.model.BudgetAlert
import com.rudra.savingbuddy.domain.model.BillingCycle
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.model.Subscription
import com.rudra.savingbuddy.domain.repository.BudgetRepository
import com.rudra.savingbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetUiState(
    val budget: Budget? = null,
    val categorySpending: Map<ExpenseCategory, Double> = emptyMap(),
    val alerts: List<BudgetAlert> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val upcomingRenewals: List<Subscription> = emptyList(),
    val isLoading: Boolean = false,
    val showAddSubscriptionDialog: Boolean = false,
    val showEditBudgetDialog: Boolean = false,
    val editingSubscription: Subscription? = null,
    val totalBudget: Double = 0.0,
    val spent: Double = 0.0,
    val remaining: Double = 0.0,
    val alertThreshold: Int = 80
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudget()
    }

    private fun loadBudget() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            budgetRepository.getBudget().collect { budget ->
                if (budget != null) {
                    val remaining = budget.monthlyLimit - 0.0
                    _uiState.update {
                        it.copy(
                            budget = budget,
                            totalBudget = budget.monthlyLimit,
                            remaining = remaining,
                            alertThreshold = budget.alertThreshold,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }

        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        viewModelScope.launch {
            val subscriptions = mutableListOf<Subscription>()
            val now = System.currentTimeMillis()
            val threeDays = now + (3 * 24 * 60 * 60 * 1000L)
            val sevenDays = now + (7 * 24 * 60 * 60 * 1000L)

            val sampleSubscriptions = listOf(
                Subscription(
                    id = 1,
                    name = "Netflix",
                    amount = 499.0,
                    billingCycle = BillingCycle.MONTHLY,
                    nextBillingDate = now + (2 * 24 * 60 * 60 * 1000L),
                    category = "Entertainment"
                ),
                Subscription(
                    id = 2,
                    name = "Spotify",
                    amount = 199.0,
                    billingCycle = BillingCycle.MONTHLY,
                    nextBillingDate = now + (5 * 24 * 60 * 60 * 1000L),
                    category = "Music"
                ),
                Subscription(
                    id = 3,
                    name = "Amazon Prime",
                    amount = 1499.0,
                    billingCycle = BillingCycle.YEARLY,
                    nextBillingDate = now + (30 * 24 * 60 * 60 * 1000L),
                    category = "Shopping"
                ),
                Subscription(
                    id = 4,
                    name = "Hotstar",
                    amount = 1499.0,
                    billingCycle = BillingCycle.YEARLY,
                    nextBillingDate = now - (10 * 24 * 60 * 60 * 1000L),
                    category = "Entertainment",
                    isActive = false
                )
            )

            val upcomingRenewals = sampleSubscriptions.filter {
                it.isActive && it.nextBillingDate in now..sevenDays
            }

            _uiState.update {
                it.copy(
                    subscriptions = sampleSubscriptions,
                    upcomingRenewals = upcomingRenewals
                )
            }
        }
    }

    fun showAddSubscriptionDialog() {
        _uiState.update { it.copy(showAddSubscriptionDialog = true, editingSubscription = null) }
    }

    fun showEditSubscriptionDialog(subscription: Subscription) {
        _uiState.update { it.copy(showAddSubscriptionDialog = true, editingSubscription = subscription) }
    }

    fun hideSubscriptionDialog() {
        _uiState.update { it.copy(showAddSubscriptionDialog = false, editingSubscription = null) }
    }

    fun saveSubscription(
        name: String,
        amount: Double,
        billingCycle: BillingCycle,
        nextBillingDate: Long,
        category: String,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            val subscription = Subscription(
                id = _uiState.value.editingSubscription?.id ?: System.currentTimeMillis(),
                name = name,
                amount = amount,
                billingCycle = billingCycle,
                nextBillingDate = nextBillingDate,
                category = category,
                isActive = isActive
            )

            val currentList = _uiState.value.subscriptions.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == subscription.id }

            if (existingIndex >= 0) {
                currentList[existingIndex] = subscription
            } else {
                currentList.add(subscription)
            }

            _uiState.update {
                it.copy(
                    subscriptions = currentList,
                    showAddSubscriptionDialog = false,
                    editingSubscription = null
                )
            }

            loadSubscriptions()
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            val updatedList = _uiState.value.subscriptions.filter { it.id != subscription.id }
            _uiState.update { it.copy(subscriptions = updatedList) }
            loadSubscriptions()
        }
    }

    fun toggleSubscriptionActive(subscription: Subscription) {
        viewModelScope.launch {
            val updatedList = _uiState.value.subscriptions.map {
                if (it.id == subscription.id) it.copy(isActive = !it.isActive) else it
            }
            _uiState.update { it.copy(subscriptions = updatedList) }
            loadSubscriptions()
        }
    }

    fun showBudgetDialog() {
        _uiState.update { it.copy(showEditBudgetDialog = true) }
    }

    fun hideBudgetDialog() {
        _uiState.update { it.copy(showEditBudgetDialog = false) }
    }

    fun setBudget(amount: Double, alertThreshold: Int, enableRollover: Boolean) {
        viewModelScope.launch {
            val budget = Budget(
                id = 1,
                monthlyLimit = amount,
                month = DateUtils.getCurrentMonth(),
                year = DateUtils.getCurrentYear(),
                alertThreshold = alertThreshold,
                enableRollover = enableRollover
            )
            budgetRepository.setBudget(budget)
            hideBudgetDialog()
        }
    }

    fun getSubscriptionReminders(): List<Pair<Subscription, Int>> {
        val reminders = mutableListOf<Pair<Subscription, Int>>()
        val now = System.currentTimeMillis()

        _uiState.value.subscriptions.filter { it.isActive }.forEach { subscription ->
            val daysUntilExpiry = ((subscription.nextBillingDate - now) / (24 * 60 * 60 * 1000L)).toInt()

            when {
                daysUntilExpiry == 3 -> reminders.add(Pair(subscription, 3))
                daysUntilExpiry == 2 -> reminders.add(Pair(subscription, 2))
                daysUntilExpiry == 1 -> reminders.add(Pair(subscription, 1))
                daysUntilExpiry <= 0 -> reminders.add(Pair(subscription, 0))
            }
        }

        return reminders
    }

    fun refreshData() {
        loadBudget()
    }
}