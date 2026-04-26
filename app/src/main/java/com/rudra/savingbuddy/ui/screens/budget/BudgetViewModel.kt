package com.rudra.savingbuddy.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Budget
import com.rudra.savingbuddy.domain.model.BudgetAlert
import com.rudra.savingbuddy.domain.model.BillingCycle
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.model.Subscription
import com.rudra.savingbuddy.domain.repository.BudgetRepository
import com.rudra.savingbuddy.domain.repository.SubscriptionRepository
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
    val subscriptionToDelete: Subscription? = null,
    val totalBudget: Double = 0.0,
    val spent: Double = 0.0,
    val remaining: Double = 0.0,
    val alertThreshold: Int = 80
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudget()
        loadSubscriptions()
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
    }

    private fun loadSubscriptions() {
        viewModelScope.launch {
            subscriptionRepository.getAllSubscriptions().collect { subscriptions ->
                val now = System.currentTimeMillis()
                val sevenDays = now + (7 * 24 * 60 * 60 * 1000L)

                val upcomingRenewals = subscriptions.filter {
                    it.isActive && it.nextBillingDate in now..sevenDays
                }

                _uiState.update {
                    it.copy(
                        subscriptions = subscriptions,
                        upcomingRenewals = upcomingRenewals
                    )
                }
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

    fun showDeleteConfirmation(subscription: Subscription) {
        _uiState.update { it.copy(subscriptionToDelete = subscription) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(subscriptionToDelete = null) }
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
                id = _uiState.value.editingSubscription?.id ?: 0,
                name = name,
                amount = amount,
                billingCycle = billingCycle,
                nextBillingDate = nextBillingDate,
                category = category,
                isActive = isActive
            )

            if (_uiState.value.editingSubscription != null) {
                subscriptionRepository.updateSubscription(subscription)
            } else {
                subscriptionRepository.insertSubscription(subscription)
            }

            _uiState.update {
                it.copy(
                    showAddSubscriptionDialog = false,
                    editingSubscription = null
                )
            }
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            subscriptionRepository.deleteSubscription(subscription.id)
            _uiState.update { it.copy(subscriptionToDelete = null) }
        }
    }

    fun confirmDelete() {
        _uiState.value.subscriptionToDelete?.let { subscription ->
            deleteSubscription(subscription)
        }
    }

    fun toggleSubscriptionActive(subscription: Subscription) {
        viewModelScope.launch {
            subscriptionRepository.updateSubscriptionActiveStatus(subscription.id, !subscription.isActive)
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
        loadSubscriptions()
    }
}