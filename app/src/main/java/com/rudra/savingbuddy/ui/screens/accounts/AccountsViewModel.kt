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
    val linkedGoals: List<Goal> = emptyList()
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
}