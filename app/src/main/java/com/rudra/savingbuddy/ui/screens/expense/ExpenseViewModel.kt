package com.rudra.savingbuddy.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.RecurringInterval
import com.rudra.savingbuddy.domain.model.RecurringStatus
import com.rudra.savingbuddy.domain.model.EndCondition
import com.rudra.savingbuddy.domain.repository.AccountRepository
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpenseUiState(
    val expenseList: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingExpense: Expense? = null,
    val quickAddCategory: ExpenseCategory? = null,
    val totalCount: Int = 0,
    val currentPage: Int = 0,
    val hasMoreItems: Boolean = true
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

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
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                expenseRepository.getExpensesPaginated(PAGE_SIZE, 0),
                expenseRepository.getExpenseCount()
            ) { list, count ->
                Pair(list, count)
            }.collect { (list, count) ->
                _uiState.update { 
                    it.copy(
                        expenseList = list, 
                        totalCount = count,
                        isLoading = false,
                        hasMoreItems = list.size >= PAGE_SIZE
                    ) 
                }
            }
        }
    }

    fun loadMoreExpenses() {
        val currentState = _uiState.value
        if (currentState.isLoading || !currentState.hasMoreItems) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val nextPage = currentState.currentPage + 1
            expenseRepository.getExpensesPaginated(PAGE_SIZE, nextPage * PAGE_SIZE).collect { newItems ->
                _uiState.update { state ->
                    state.copy(
                        expenseList = state.expenseList + newItems,
                        currentPage = nextPage,
                        isLoading = false,
                        hasMoreItems = newItems.size >= PAGE_SIZE
                    )
                }
            }
        }
    }

    fun refreshExpenses() {
        _uiState.update { it.copy(currentPage = 0, hasMoreItems = true) }
        loadExpenses()
    }

    fun quickAdd(category: ExpenseCategory, amount: Double, accountId: Long? = null) {
        viewModelScope.launch {
            try {
                val expense = Expense(
                    amount = amount,
                    category = category,
                    date = System.currentTimeMillis(),
                    accountId = accountId
                )
                expenseRepository.insertExpense(expense, deductFromAccount = accountId != null)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingExpense = null) }
    }

    fun showEditDialog(expense: Expense) {
        _uiState.update { it.copy(showAddDialog = true, editingExpense = expense) }
    }

    fun hideDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingExpense = null) }
    }

    fun saveExpense(
        amount: Double,
        category: ExpenseCategory,
        date: Long,
        notes: String?,
        accountId: Long?,
        isRecurring: Boolean = false,
        recurringInterval: RecurringInterval? = null
    ) {
        viewModelScope.launch {
            try {
                val expense = Expense(
                    id = _uiState.value.editingExpense?.id ?: 0,
                    amount = amount,
                    category = category,
                    date = date,
                    notes = notes,
                    accountId = accountId,
                    isRecurring = isRecurring,
                    recurringInterval = if (isRecurring) recurringInterval else null
                )
                
                if (_uiState.value.editingExpense != null) {
                    expenseRepository.updateExpense(expense)
                } else {
                    expenseRepository.insertExpense(expense, deductFromAccount = accountId != null)
                }
                hideDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                expenseRepository.deleteExpense(expense.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun filterByCategory(category: ExpenseCategory?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (category == null) {
                expenseRepository.getExpensesPaginated(PAGE_SIZE, 0).collect { list ->
                    _uiState.update { it.copy(expenseList = list, isLoading = false) }
                }
            } else {
                expenseRepository.getExpensesPaginated(PAGE_SIZE, 0).collect { list ->
                    val filtered = list.filter { it.category == category }
                    _uiState.update { it.copy(expenseList = filtered, isLoading = false) }
                }
            }
        }
    }

    fun filterByDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            expenseRepository.getExpensesByDateRange(startDate, endDate).collect { list ->
                _uiState.update { it.copy(expenseList = list, isLoading = false) }
            }
        }
    }

    fun searchExpenses(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            expenseRepository.getExpensesPaginated(1000, 0).collect { list ->
                val filtered = if (query.isBlank()) {
                    list
                } else {
                    list.filter { 
                        it.category.displayName.contains(query, ignoreCase = true) ||
                        it.notes?.contains(query, ignoreCase = true) == true
                    }
                }
                _uiState.update { it.copy(expenseList = filtered, isLoading = false) }
            }
        }
    }
}