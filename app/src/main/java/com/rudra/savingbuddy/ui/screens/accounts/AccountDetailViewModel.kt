package com.rudra.savingbuddy.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.Transfer
import com.rudra.savingbuddy.domain.model.TransferResult
import com.rudra.savingbuddy.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountDetailUiState(
    val account: Account? = null,
    val transfers: List<Transfer> = emptyList(),
    val allAccounts: List<Account> = emptyList(),
    val balanceHistory: List<Double> = emptyList(),
    val isLoading: Boolean = false,
    val showAddMoneyDialog: Boolean = false,
    val showCashOutDialog: Boolean = false,
    val addMoneyAmount: String = "",
    val cashOutAmount: String = "",
    val operationResult: TransferResult? = null,
    val error: String? = null,
    val showLimitDialog: Boolean = false,
    val limitEditAmount: String = "",
    val limitEditEnabled: Boolean = false
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    private var currentAccountId: Long = 0

    fun loadAccount(accountId: Long) {
        currentAccountId = accountId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            accountRepository.getAccountById(accountId).collect { account ->
                _uiState.update { it.copy(account = account) }
            }
        }

        viewModelScope.launch {
            accountRepository.getAllAccounts().collect { accounts ->
                _uiState.update { it.copy(allAccounts = accounts) }
            }
        }

        viewModelScope.launch {
            val transfers = accountRepository.getTransfersForAccount(accountId)
            _uiState.update { it.copy(transfers = transfers, isLoading = false) }
        }

        viewModelScope.launch {
            accountRepository.getBalanceHistory(accountId).collect { history ->
                val balances = history.map { it.balance }
                _uiState.update { it.copy(balanceHistory = balances) }
            }
        }
    }

    fun showAddMoneyDialog() {
        _uiState.update { it.copy(showAddMoneyDialog = true, addMoneyAmount = "", error = null) }
    }

    fun hideAddMoneyDialog() {
        _uiState.update { it.copy(showAddMoneyDialog = false, addMoneyAmount = "") }
    }

    fun showCashOutDialog() {
        _uiState.update { it.copy(showCashOutDialog = true, cashOutAmount = "", error = null) }
    }

    fun hideCashOutDialog() {
        _uiState.update { it.copy(showCashOutDialog = false, cashOutAmount = "") }
    }

    fun updateAddMoneyAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(addMoneyAmount = amount) }
        }
    }

    fun updateCashOutAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(cashOutAmount = amount) }
        }
    }

    fun addMoney() {
        val amount = _uiState.value.addMoneyAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = accountRepository.addMoneyToAccount(currentAccountId, amount, "Deposit")
            if (result.success) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        showAddMoneyDialog = false,
                        operationResult = result,
                        addMoneyAmount = ""
                    ) 
                }
                loadAccount(currentAccountId)
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.errorMessage) }
            }
        }
    }

    fun cashOut() {
        val amount = _uiState.value.cashOutAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = accountRepository.withdrawMoneyFromAccount(currentAccountId, amount, "Withdrawal")
            if (result.success) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        showCashOutDialog = false,
                        operationResult = result,
                        cashOutAmount = ""
                    ) 
                }
                loadAccount(currentAccountId)
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.errorMessage) }
            }
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(operationResult = null) }
    }

    fun showLimitDialog() {
        val account = _uiState.value.account
        val hasLimit = account?.dailyLimit != null && account.dailyLimit!! > 0
        _uiState.update {
            it.copy(
                showLimitDialog = true,
                limitEditAmount = if (hasLimit) account.dailyLimit.toString() else "",
                limitEditEnabled = hasLimit,
                error = null
            )
        }
    }

    fun hideLimitDialog() {
        _uiState.update { it.copy(showLimitDialog = false, limitEditAmount = "", error = null) }
    }

    fun updateLimitEditAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(limitEditAmount = amount) }
        }
    }

    fun toggleLimitEdit(enabled: Boolean) {
        _uiState.update {
            it.copy(
                limitEditEnabled = enabled,
                limitEditAmount = if (!enabled) "" else it.limitEditAmount
            )
        }
    }

    fun saveLimit() {
        val state = _uiState.value
        val account = state.account ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val newLimit = if (state.limitEditEnabled) state.limitEditAmount.toDoubleOrNull()?.takeIf { it > 0 } else null
                val updatedAccount = account.copy(
                    dailyLimit = newLimit,
                    lastUpdated = System.currentTimeMillis()
                )
                accountRepository.updateAccount(updatedAccount)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showLimitDialog = false,
                        limitEditAmount = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to save limit")
                }
            }
        }
    }
}