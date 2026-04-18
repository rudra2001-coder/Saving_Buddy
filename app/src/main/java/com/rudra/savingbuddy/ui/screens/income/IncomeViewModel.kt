package com.rudra.savingbuddy.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.IncomeCategory
import com.rudra.savingbuddy.domain.model.RecurringInterval
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
    val editingIncome: Income? = null
)

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

    init {
        loadIncome()
    }

    private fun loadIncome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            incomeRepository.getAllIncome().collect { list ->
                _uiState.update { it.copy(incomeList = list, isLoading = false) }
            }
        }
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
        notes: String?
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
                    notes = notes
                )
                
                if (_uiState.value.editingIncome != null) {
                    incomeRepository.updateIncome(income)
                } else {
                    incomeRepository.insertIncome(income)
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
}