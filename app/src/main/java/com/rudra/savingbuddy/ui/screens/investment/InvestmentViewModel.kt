package com.rudra.savingbuddy.ui.screens.investment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Investment
import com.rudra.savingbuddy.domain.model.InvestmentType
import com.rudra.savingbuddy.domain.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvestmentUiState(
    val investments: List<Investment> = emptyList(),
    val selectedType: InvestmentType? = null,
    val showAddDialog: Boolean = false,
    val editingInvestment: Investment? = null,
    val showTrackDialog: Investment? = null,
    val showDeleteConfirm: Investment? = null,
    val isLoading: Boolean = false
) {
    val filteredInvestments: List<Investment>
        get() = if (selectedType == null) investments
        else investments.filter { it.type == selectedType }

    val totalInvested: Double get() = investments.sumOf { it.amount }
    val totalCurrentValue: Double get() = investments.sumOf { it.currentValue }
    val totalReturns: Double get() = totalCurrentValue - totalInvested
    val overallReturnRate: Double
        get() = if (totalInvested > 0) (totalReturns / totalInvested) * 100 else 0.0
}

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentUiState())
    val uiState: StateFlow<InvestmentUiState> = _uiState.asStateFlow()

    init {
        loadInvestments()
    }

    private fun loadInvestments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            investmentRepository.getAllInvestments().collect { investments ->
                _uiState.update { it.copy(investments = investments, isLoading = false) }
            }
        }
    }

    fun selectType(type: InvestmentType?) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingInvestment = null) }
    }

    fun showEditDialog(investment: Investment) {
        _uiState.update { it.copy(showAddDialog = true, editingInvestment = investment) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingInvestment = null) }
    }

    fun saveInvestment(name: String, type: InvestmentType, amount: Double, currentValue: Double, purchaseDate: Long, notes: String?) {
        viewModelScope.launch {
            val existing = _uiState.value.editingInvestment
            if (existing != null) {
                investmentRepository.updateInvestment(
                    existing.copy(
                        name = name,
                        type = type,
                        amount = amount,
                        currentValue = currentValue,
                        purchaseDate = purchaseDate,
                        notes = notes
                    )
                )
            } else {
                investmentRepository.insertInvestment(
                    Investment(
                        name = name,
                        type = type,
                        amount = amount,
                        currentValue = currentValue,
                        purchaseDate = purchaseDate,
                        notes = notes
                    )
                )
            }
            hideAddDialog()
        }
    }

    fun showTrackDialog(investment: Investment) {
        _uiState.update { it.copy(showTrackDialog = investment) }
    }

    fun hideTrackDialog() {
        _uiState.update { it.copy(showTrackDialog = null) }
    }

    fun updateCurrentValue(investment: Investment, newValue: Double) {
        viewModelScope.launch {
            investmentRepository.updateInvestment(investment.copy(currentValue = newValue))
            hideTrackDialog()
        }
    }

    fun showDeleteConfirm(investment: Investment) {
        _uiState.update { it.copy(showDeleteConfirm = investment) }
    }

    fun hideDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = null) }
    }

    fun deleteInvestment(investment: Investment) {
        viewModelScope.launch {
            investmentRepository.deleteInvestment(investment.id)
            hideDeleteConfirm()
        }
    }
}
