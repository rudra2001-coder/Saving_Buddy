package com.rudra.savingbuddy.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.AccountProvider
import com.rudra.savingbuddy.domain.model.AccountType
import com.rudra.savingbuddy.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class AddAccountUiState(
    val selectedType: AccountType? = null,
    val selectedProvider: AccountProvider? = null,
    val customProviderName: String = "",
    val accountNumber: String = "",
    val nickname: String = "",
    val initialBalance: String = "0",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val canSave: Boolean
        get() = selectedType != null && 
                selectedProvider != null && 
                accountNumber.isNotBlank() &&
                (selectedProvider?.isCustom != true || customProviderName.isNotBlank())
    
    val showCustomProviderName: Boolean
        get() = selectedProvider?.isCustom == true
}

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()

    fun selectType(type: AccountType) {
        _uiState.update { 
            it.copy(
                selectedType = type, 
                selectedProvider = null,
                error = null
            ) 
        }
    }

    fun selectProvider(provider: AccountProvider) {
        _uiState.update { it.copy(selectedProvider = provider, error = null) }
    }

    fun updateAccountNumber(number: String) {
        _uiState.update { it.copy(accountNumber = number, error = null) }
    }

    fun updateNickname(name: String) {
        _uiState.update { it.copy(nickname = name) }
    }

    fun updateInitialBalance(balance: String) {
        if (balance.isEmpty() || balance.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(initialBalance = balance) }
        }
    }

    fun updateCustomProviderName(name: String) {
        _uiState.update { it.copy(customProviderName = name) }
    }

    fun saveAccount() {
        val state = _uiState.value
        
        if (!state.canSave) {
            _uiState.update { it.copy(error = "Please fill all required fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val initialBalance = state.initialBalance.toDoubleOrNull() ?: 0.0
                val providerDisplayName = if (state.selectedProvider?.isCustom == true && state.customProviderName.isNotBlank()) {
                    state.customProviderName
                } else {
                    state.selectedProvider?.displayName ?: "Account"
                }
                val account = Account(
                    name = state.nickname.ifBlank { providerDisplayName },
                    type = state.selectedType!!,
                    provider = if (state.selectedProvider?.isCustom == true && state.customProviderName.isNotBlank()) {
                        state.customProviderName.uppercase().replace(" ", "_")
                    } else {
                        state.selectedProvider!!.name
                    },
                    accountNumber = state.accountNumber,
                    balance = initialBalance,
                    initialBalance = initialBalance,
                    currency = "BDT",
                    iconColor = generateIconColor(),
                    dailyLimit = state.selectedProvider!!.dailyTransferLimit.takeIf { it > 0 }
                )

                accountRepository.insertAccount(account)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to save account"
                    ) 
                }
            }
        }
    }

    private fun generateIconColor(): Long {
        val colors = listOf(
            0xFF1565C0, // Blue
            0xFF2E7D32, // Green
            0xFFFF6D00, // Orange
            0xFFD32F2F, // Red
            0xFF7B1FA2, // Purple
            0xFF00796B, // Teal
            0xFFF57C00, // Amber
            0xFF5D4037  // Brown
        )
        return colors[Random.nextInt(colors.size)]
    }
}