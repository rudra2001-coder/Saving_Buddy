package com.rudra.savingbuddy.ui.screens.fusion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.*
import com.rudra.savingbuddy.domain.repository.FusionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FusionUiState(
    val unifiedTransactions: List<UnifiedTransaction> = emptyList(),
    val netWorthSummary: NetWorthSummary? = null,
    val accountHealthList: List<AccountHealth> = emptyList(),
    val transferPatterns: List<TransferPattern> = emptyList(),
    val insights: List<FusionInsight> = emptyList(),
    val goalSuggestions: List<GoalFundingSuggestion> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreTransactions: Boolean = true,
    val error: String? = null,
    val showAllAccounts: Boolean = false,
    val selectedAccountId: Long? = null,
    val currentPage: Int = 0
)

sealed class FusionEvent {
    data class ToggleAllAccounts(val showAll: Boolean) : FusionEvent()
    data class SelectAccount(val accountId: Long?) : FusionEvent()
    data class AllocateToGoal(val goalId: Long, val amount: Double, val fromAccountId: Long) : FusionEvent()
    object Refresh : FusionEvent()
    object LoadMore : FusionEvent()
    object ClearError : FusionEvent()
}

@HiltViewModel
class FusionViewModel @Inject constructor(
    private val fusionRepository: FusionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FusionUiState())
    val uiState: StateFlow<FusionUiState> = _uiState.asStateFlow()

    private val _showAllAccounts = MutableStateFlow(false)
    private val _selectedAccountId = MutableStateFlow<Long?>(null)
    private var allTransactionsCache = mutableListOf<UnifiedTransaction>()

    companion object {
        private const val PAGE_SIZE = 50
    }

    init {
        loadFusionData()
    }

    private fun loadFusionData() {
        viewModelScope.launch {
            combine(
                fusionRepository.getUnifiedTransactions(PAGE_SIZE),
                fusionRepository.getNetWorthSummary(),
                fusionRepository.getAccountHealthList(),
                fusionRepository.getTransferPatterns(),
                fusionRepository.getFusionInsights(),
                fusionRepository.getGoalFundingSuggestions(),
                _showAllAccounts,
                _selectedAccountId
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                allTransactionsCache = (values[0] as List<UnifiedTransaction>).toMutableList()
                FusionUiState(
                    unifiedTransactions = allTransactionsCache.take(PAGE_SIZE),
                    netWorthSummary = values[1] as NetWorthSummary,
                    accountHealthList = values[2] as List<AccountHealth>,
                    transferPatterns = values[3] as List<TransferPattern>,
                    insights = values[4] as List<FusionInsight>,
                    goalSuggestions = values[5] as List<GoalFundingSuggestion>,
                    showAllAccounts = values[6] as Boolean,
                    selectedAccountId = values[7] as Long?,
                    isLoading = false,
                    hasMoreTransactions = allTransactionsCache.size > PAGE_SIZE,
                    currentPage = 1
                )
            }.catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: FusionEvent) {
        when (event) {
            is FusionEvent.ToggleAllAccounts -> {
                _showAllAccounts.value = event.showAll
                _selectedAccountId.value = null
            }
            is FusionEvent.SelectAccount -> {
                _selectedAccountId.value = event.accountId
                _showAllAccounts.value = false
            }
            is FusionEvent.AllocateToGoal -> {
                viewModelScope.launch {
                    fusionRepository.allocateToGoal(event.goalId, event.amount, event.fromAccountId)
                }
            }
            is FusionEvent.Refresh -> {
                refreshData()
            }
            is FusionEvent.LoadMore -> {
                loadMoreTransactions()
            }
            is FusionEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            try {
                val newTransactions = fusionRepository.getUnifiedTransactions(PAGE_SIZE * (_uiState.value.currentPage + 1)).first()
                allTransactionsCache = newTransactions.toMutableList()
                
                _uiState.update { state ->
                    state.copy(
                        unifiedTransactions = getFilteredTransactionsList(allTransactionsCache),
                        isRefreshing = false,
                        hasMoreTransactions = allTransactionsCache.size > PAGE_SIZE * state.currentPage
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRefreshing = false, error = e.message) }
            }
        }
    }

    private fun loadMoreTransactions() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMoreTransactions) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            
            try {
                val nextPage = _uiState.value.currentPage + 1
                val newTransactions = fusionRepository.getUnifiedTransactions(PAGE_SIZE * nextPage).first()
                allTransactionsCache = newTransactions.toMutableList()
                
                _uiState.update { state ->
                    state.copy(
                        unifiedTransactions = getFilteredTransactionsList(allTransactionsCache),
                        isLoadingMore = false,
                        hasMoreTransactions = newTransactions.size > PAGE_SIZE * nextPage,
                        currentPage = nextPage
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun getFilteredTransactions(): List<UnifiedTransaction> {
        return getFilteredTransactionsList(allTransactionsCache)
    }

    private fun getFilteredTransactionsList(transactions: List<UnifiedTransaction>): List<UnifiedTransaction> {
        val state = _uiState.value
        return if (state.showAllAccounts) {
            transactions
        } else if (state.selectedAccountId != null) {
            transactions.filter { 
                it.accountId == state.selectedAccountId || it.relatedAccountId == state.selectedAccountId 
            }
        } else {
            transactions.take(PAGE_SIZE)
        }
    }
}