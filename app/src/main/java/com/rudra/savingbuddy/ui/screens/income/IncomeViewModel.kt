package com.rudra.savingbuddy.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.IncomeCategory
import com.rudra.savingbuddy.domain.model.RecurringInterval
import com.rudra.savingbuddy.domain.repository.AccountRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IncomeUiState(
    val incomeList: List<Income> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingIncome: Income? = null,
    val totalCount: Int = 0,
    val currentPage: Int = 0,
    val hasMoreItems: Boolean = true
)

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    fun loadAccountsForSelection(onAccountsLoaded: (List<Account>) -> Unit) {
        viewModelScope.launch {
            accountRepository.getAllAccounts().collect { accounts ->
                onAccountsLoaded(accounts)
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 100
    }

    init {
        loadIncome()
    }

    private fun loadIncome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                incomeRepository.getIncomePaginated(PAGE_SIZE, 0),
                incomeRepository.getIncomeCount()
            ) { list, count ->
                Pair(list, count)
            }.collect { (list, count) ->
                _uiState.update { 
                    it.copy(
                        incomeList = list, 
                        totalCount = count,
                        isLoading = false,
                        hasMoreItems = list.size >= PAGE_SIZE
                    ) 
                }
            }
        }
    }

    fun loadMoreIncome() {
        val currentState = _uiState.value
        if (currentState.isLoading || !currentState.hasMoreItems) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val nextPage = currentState.currentPage + 1
            incomeRepository.getIncomePaginated(PAGE_SIZE, nextPage * PAGE_SIZE).collect { newItems ->
                _uiState.update { state ->
                    state.copy(
                        incomeList = state.incomeList + newItems,
                        currentPage = nextPage,
                        isLoading = false,
                        hasMoreItems = newItems.size >= PAGE_SIZE
                    )
                }
            }
        }
    }

    fun refreshIncome() {
        _uiState.update { it.copy(currentPage = 0, hasMoreItems = true) }
        loadIncome()
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingIncome = null) }
    }

    fun showEditDialog(income: Income) {
        _uiState.update { it.copy(showAddDialog = true, editingIncome = income) }
    }

    fun hideDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingIncome = null) }
    }

    fun saveIncome(
        source: String,
        amount: Double,
        category: IncomeCategory,
        date: Long,
        isRecurring: Boolean,
        recurringInterval: RecurringInterval?,
        notes: String?,
        accountId: Long?
    ) {
        viewModelScope.launch {
            try {
                val income = Income(
                    id = _uiState.value.editingIncome?.id ?: 0,
                    source = source,
                    amount = amount,
                    category = category,
                    date = date,
                    isRecurring = isRecurring,
                    recurringInterval = if (isRecurring) recurringInterval else null,
                    notes = notes,
                    accountId = accountId
                )
                
                if (_uiState.value.editingIncome != null) {
                    incomeRepository.updateIncome(income)
                } else {
                    incomeRepository.insertIncome(income, addToWallet = accountId != null)
                }
                hideDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            try {
                incomeRepository.deleteIncome(income.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun searchIncome(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            incomeRepository.getIncomePaginated(1000, 0).collect { list ->
                val filtered = if (query.isBlank()) {
                    list
                } else {
                    list.filter { 
                        it.source.contains(query, ignoreCase = true) ||
                        it.category.displayName.contains(query, ignoreCase = true) ||
                        it.notes?.contains(query, ignoreCase = true) == true
                    }
                }
                _uiState.update { it.copy(incomeList = filtered, isLoading = false) }
            }
        }
    }

    fun filterByRecurring(isRecurringOnly: Boolean?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            incomeRepository.getIncomePaginated(1000, 0).collect { list ->
                val filtered = when (isRecurringOnly) {
                    true -> list.filter { it.isRecurring }
                    false -> list.filter { !it.isRecurring }
                    null -> list
                }
                _uiState.update { it.copy(incomeList = filtered, isLoading = false) }
            }
        }
    }

    fun filterByCategory(category: IncomeCategory?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            incomeRepository.getIncomePaginated(1000, 0).collect { list ->
                val filtered = if (category == null) {
                    list
                } else {
                    list.filter { it.category == category }
                }
                _uiState.update { it.copy(incomeList = filtered, isLoading = false) }
            }
        }
    }
}