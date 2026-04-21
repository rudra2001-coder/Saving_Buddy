package com.rudra.savingbuddy.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.TransferResult
import com.rudra.savingbuddy.domain.repository.AccountRepository
import com.rudra.savingbuddy.domain.repository.FusionRepository
import com.rudra.savingbuddy.domain.model.GoalFundingSuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferUiState(
    val availableAccounts: List<Account> = emptyList(),
    val fromAccount: Account? = null,
    val toAccount: Account? = null,
    val amount: String = "",
    val note: String = "",
    val fee: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val transferResult: TransferResult? = null,
    val showGoalSuggestion: Boolean = false,
    val goalSuggestion: GoalFundingSuggestion? = null,
    val dailyLimitUsage: Double = 0.0,
    val dailyLimit: Double = 0.0
) {
    val totalAmount: Double
        get() = (amount.toDoubleOrNull() ?: 0.0) + fee

    val canTransfer: Boolean
        get() = fromAccount != null && 
                toAccount != null && 
                amount.isNotBlank() && 
                (amount.toDoubleOrNull() ?: 0.0) > 0 &&
                fromAccount?.id != toAccount?.id

    val dailyLimitPercent: Float
        get() = if (dailyLimit > 0) (dailyLimitUsage / dailyLimit).toFloat().coerceIn(0f, 1f) else 0f
}

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val fusionRepository: FusionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAllAccounts().collect { accounts ->
                _uiState.update { it.copy(availableAccounts = accounts) }
            }
        }
    }

    fun setFromAccount(account: Account) {
        viewModelScope.launch {
            val dailyTotal = accountRepository.getDailyTransferTotal(account.id)
            val provider = com.rudra.savingbuddy.domain.model.AccountProvider.entries
                .find { it.name == account.provider }
            
            _uiState.update { 
                it.copy(
                    fromAccount = account, 
                    error = null,
                    dailyLimitUsage = dailyTotal,
                    dailyLimit = provider?.dailyTransferLimit ?: 0.0
                ) 
            }
        }
    }

    fun setToAccount(account: Account) {
        _uiState.update { it.copy(toAccount = account, error = null) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount, error = null) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun executeTransfer() {
        val state = _uiState.value
        
        if (state.fromAccount == null || state.toAccount == null) {
            _uiState.update { it.copy(error = "Please select both accounts") }
            return
        }

        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        if (state.fromAccount.id == state.toAccount.id) {
            _uiState.update { it.copy(error = "Cannot transfer to the same account") }
            return
        }

        if (state.fromAccount.balance < amount) {
            _uiState.update { it.copy(error = "Insufficient balance") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = accountRepository.transferMoney(
                    fromId = state.fromAccount.id,
                    toId = state.toAccount.id,
                    amount = amount,
                    note = state.note.ifBlank { null }
                )

                if (result.success) {
                    accountRepository.getAllAccounts().first().let { accounts ->
                        val fromAcc = accounts.find { it.id == state.fromAccount.id }
                        val toAcc = accounts.find { it.id == state.toAccount.id }
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                transferResult = result,
                                availableAccounts = accounts,
                                fromAccount = fromAcc,
                                toAccount = toAcc
                            )
                        }
                    }
                    
                    loadGoalSuggestion(state.toAccount.id, amount)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = result.errorMessage ?: "Transfer failed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    private fun loadGoalSuggestion(toAccountId: Long, transferredAmount: Double) {
        viewModelScope.launch {
            fusionRepository.getGoalFundingSuggestions().first().let { suggestions ->
                if (suggestions.isNotEmpty()) {
                    val suggestion = suggestions.firstOrNull { it.fromAccountId == toAccountId }
                    if (suggestion != null && suggestion.suggestedAmount <= transferredAmount * 0.5) {
                        _uiState.update {
                            it.copy(
                                showGoalSuggestion = true,
                                goalSuggestion = suggestion
                            )
                        }
                    }
                }
            }
        }
    }

    fun dismissGoalSuggestion() {
        _uiState.update { it.copy(showGoalSuggestion = false, goalSuggestion = null) }
    }

    fun allocateToGoalFromSuggestion() {
        val suggestion = _uiState.value.goalSuggestion ?: return
        
        viewModelScope.launch {
            fusionRepository.allocateToGoal(
                suggestion.goalId,
                suggestion.suggestedAmount,
                suggestion.fromAccountId
            )
            _uiState.update { it.copy(showGoalSuggestion = false, goalSuggestion = null) }
        }
    }

    fun reset() {
        _uiState.update { 
            TransferUiState(availableAccounts = it.availableAccounts) 
        }
    }
}