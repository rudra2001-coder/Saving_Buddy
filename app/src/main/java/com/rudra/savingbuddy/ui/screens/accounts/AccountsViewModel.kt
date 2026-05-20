package com.rudra.savingbuddy.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.*
import com.rudra.savingbuddy.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val wallets: List<Account> = emptyList(),
    val bankAccounts: List<Account> = emptyList(),
    val mobileBanking: List<Account> = emptyList(),
    val digitalWallets: List<Account> = emptyList(),
    val netWorth: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val linkedGoals: List<Goal> = emptyList(),
    val swipeTargetAccount: Account? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val showTransferDialog: Boolean = false,
    val showDeleteSuccess: Boolean = false,
    val otherAccounts: List<Account> = emptyList(),
    val transferTargetAccount: Account? = null,
    val isProcessing: Boolean = false
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            accountRepository.getAllAccounts().collect { accounts ->
                val wallets = accounts.filter { it.type == AccountType.WALLET }
                val banks = accounts.filter { it.type == AccountType.BANK }
                val mobile = accounts.filter { it.type == AccountType.MOBILE_BANKING }
                val digital = accounts.filter { it.type == AccountType.DIGITAL_WALLET }

                _uiState.update {
                    it.copy(
                        accounts = accounts,
                        wallets = wallets,
                        bankAccounts = banks,
                        mobileBanking = mobile,
                        digitalWallets = digital,
                        netWorth = accounts.sumOf { acc -> acc.balance },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh() {
        loadAccounts()
    }

    fun onSwipeDelete(account: Account) {
        _uiState.update {
            it.copy(
                swipeTargetAccount = account,
                otherAccounts = it.accounts.filter { a -> a.id != account.id && a.id != 0L },
                showTransferDialog = account.balance > 0,
                showDeleteConfirmDialog = account.balance <= 0
            )
        }
    }

    fun onSwipeEdit(account: Account) {
        _uiState.update {
            it.copy(swipeTargetAccount = account)
        }
    }

    fun dismissDialogs() {
        _uiState.update {
            it.copy(
                swipeTargetAccount = null,
                showDeleteConfirmDialog = false,
                showTransferDialog = false,
                showDeleteSuccess = false,
                otherAccounts = emptyList(),
                transferTargetAccount = null,
                isProcessing = false,
                error = null
            )
        }
    }

    fun confirmDeleteZeroBalance() {
        val account = _uiState.value.swipeTargetAccount ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                accountRepository.deleteAccount(account.id)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        showDeleteConfirmDialog = false,
                        showDeleteSuccess = true,
                        swipeTargetAccount = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message ?: "Failed to delete account"
                    )
                }
            }
        }
    }

    fun selectTransferTarget(account: Account) {
        _uiState.update { it.copy(transferTargetAccount = account) }
    }

    fun confirmDeleteWithTransfer() {
        val sourceAccount = _uiState.value.swipeTargetAccount ?: return
        val targetAccount = _uiState.value.transferTargetAccount ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                val result = accountRepository.deleteAccountWithTransfer(sourceAccount.id, targetAccount.id)
                if (result.success) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            showTransferDialog = false,
                            showDeleteSuccess = true,
                            swipeTargetAccount = null,
                            transferTargetAccount = null,
                            otherAccounts = emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = result.errorMessage ?: "Transfer failed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message ?: "Failed to transfer and delete account"
                    )
                }
            }
        }
    }

    fun dismissDeleteSuccess() {
        _uiState.update { it.copy(showDeleteSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
