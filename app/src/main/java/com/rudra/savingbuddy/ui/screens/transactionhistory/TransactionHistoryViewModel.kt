package com.rudra.savingbuddy.ui.screens.transactionhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import com.rudra.savingbuddy.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionHistoryUiState(
    val allTransactions: List<TransactionItem> = emptyList(),
    val filteredTransactions: List<TransactionItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSavings: Double = 0.0,
    val startDate: Long? = null,
    val endDate: Long? = null
)

enum class TransactionFilter(val displayName: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSE("Expense"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom")
}

data class TransactionItem(
    val id: Long,
    val type: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val description: String = ""
)

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    init {
        loadAllTransactions()
    }

    private fun loadAllTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val startOfMonth = DateUtils.getStartOfMonth(System.currentTimeMillis())
            val endOfMonth = DateUtils.getEndOfMonth(System.currentTimeMillis())

            combine(
                incomeRepository.getIncomeByDateRange(startOfMonth, endOfMonth),
                expenseRepository.getExpensesByDateRange(startOfMonth, endOfMonth)
            ) { incomes, expenses ->
                val transactions = mutableListOf<TransactionItem>()
                
                incomes.forEach { income ->
                    transactions.add(
                        TransactionItem(
                            id = income.id,
                            type = "INCOME",
                            title = income.source,
                            amount = income.amount,
                            category = income.category.displayName,
                            date = income.date,
                            description = income.notes ?: ""
                        )
                    )
                }
                
                expenses.forEach { expense ->
                    transactions.add(
                        TransactionItem(
                            id = expense.id,
                            type = "EXPENSE",
                            title = expense.category.displayName,
                            amount = expense.amount,
                            category = expense.category.displayName,
                            date = expense.date,
                            description = expense.notes ?: ""
                        )
                    )
                }
                
                transactions.sortedByDescending { it.date }
            }.collect { transactions ->
                val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExpenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                
                _uiState.update { state ->
                    state.copy(
                        allTransactions = transactions,
                        filteredTransactions = applyFilters(transactions, state.selectedFilter, state.searchQuery),
                        isLoading = false,
                        totalIncome = totalIncome,
                        totalExpenses = totalExpenses,
                        totalSavings = totalIncome - totalExpenses
                    )
                }
            }
        }
    }

    fun setFilter(filter: TransactionFilter) {
        _uiState.update { state ->
            val (startDate, endDate) = when (filter) {
                TransactionFilter.TODAY -> {
                    val now = System.currentTimeMillis()
                    DateUtils.getStartOfDay(now) to DateUtils.getEndOfDay(now)
                }
                TransactionFilter.THIS_WEEK -> {
                    val now = System.currentTimeMillis()
                    (now - (7 * 24 * 60 * 60 * 1000L)) to now
                }
                TransactionFilter.THIS_MONTH -> {
                    val now = System.currentTimeMillis()
                    DateUtils.getStartOfMonth(now) to DateUtils.getEndOfMonth(now)
                }
                else -> null to null
            }
            
            state.copy(
                selectedFilter = filter,
                startDate = startDate,
                endDate = endDate,
                filteredTransactions = applyFilters(state.allTransactions, filter, state.searchQuery, startDate, endDate)
            )
        }
    }

    fun setCustomDateRange(startDate: Long, endDate: Long) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = TransactionFilter.CUSTOM,
                startDate = startDate,
                endDate = endDate,
                filteredTransactions = applyFilters(
                    state.allTransactions, 
                    TransactionFilter.CUSTOM, 
                    state.searchQuery, 
                    startDate, 
                    endDate
                )
            )
        }
    }

    fun clearDateRange() {
        _uiState.update { state ->
            state.copy(
                selectedFilter = TransactionFilter.ALL,
                startDate = null,
                endDate = null,
                filteredTransactions = applyFilters(
                    state.allTransactions, 
                    TransactionFilter.ALL, 
                    state.searchQuery, 
                    null, 
                    null
                )
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredTransactions = applyFilters(
                    state.allTransactions, 
                    state.selectedFilter, 
                    query,
                    state.startDate,
                    state.endDate
                )
            )
        }
    }

    private fun applyFilters(
        transactions: List<TransactionItem>,
        filter: TransactionFilter,
        searchQuery: String,
        customStartDate: Long? = null,
        customEndDate: Long? = null
    ): List<TransactionItem> {
        val now = System.currentTimeMillis()
        val startOfToday = DateUtils.getStartOfDay(now)
        val endOfToday = DateUtils.getEndOfDay(now)
        val startOfWeek = now - (7 * 24 * 60 * 60 * 1000L)
        val startOfMonth = DateUtils.getStartOfMonth(now)

        var filtered = when (filter) {
            TransactionFilter.ALL -> transactions
            TransactionFilter.INCOME -> transactions.filter { it.type == "INCOME" }
            TransactionFilter.EXPENSE -> transactions.filter { it.type == "EXPENSE" }
            TransactionFilter.TODAY -> transactions.filter { it.date in startOfToday..endOfToday }
            TransactionFilter.THIS_WEEK -> transactions.filter { it.date >= startOfWeek }
            TransactionFilter.THIS_MONTH -> transactions.filter { it.date >= startOfMonth }
            TransactionFilter.CUSTOM -> {
                if (customStartDate != null && customEndDate != null) {
                    transactions.filter { it.date in customStartDate..customEndDate }
                } else {
                    transactions
                }
            }
        }

        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }

        return filtered
    }

    fun refreshData() {
        loadAllTransactions()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}