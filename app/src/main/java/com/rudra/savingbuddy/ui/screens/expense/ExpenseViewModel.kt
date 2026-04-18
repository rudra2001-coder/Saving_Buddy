package com.rudra.savingbuddy.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory
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
    val quickAddCategory: ExpenseCategory? = null
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            expenseRepository.getAllExpenses().collect { list ->
                _uiState.update { it.copy(expenseList = list, isLoading = false) }
            }
        }
    }

    fun quickAdd(category: ExpenseCategory, amount: Double) {
        viewModelScope.launch {
            try {
                val expense = Expense(
                    amount = amount,
                    category = category,
                    date = System.currentTimeMillis()
                )
                expenseRepository.insertExpense(expense)
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
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                val expense = Expense(
                    id = _uiState.value.editingExpense?.id ?: 0,
                    amount = amount,
                    category = category,
                    date = date,
                    notes = notes
                )
                
                if (_uiState.value.editingExpense != null) {
                    expenseRepository.updateExpense(expense)
                } else {
                    expenseRepository.insertExpense(expense)
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
}