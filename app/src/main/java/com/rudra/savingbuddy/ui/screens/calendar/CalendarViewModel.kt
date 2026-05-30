package com.rudra.savingbuddy.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.BillCycle
import com.rudra.savingbuddy.domain.model.BillReminder
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.repository.BillReminderRepository
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class DayTransactionSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val hasIncome: Boolean = false,
    val hasExpense: Boolean = false,
    val transactionCount: Int = 0,
    val billCount: Int = 0
)

data class BillWithDate(
    val bill: BillReminder,
    val dueDate: Long
)

data class CalendarUiState(
    val expenses: List<Expense> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val bills: List<BillReminder> = emptyList(),
    val billsByDate: Map<Long, List<BillReminder>> = emptyMap(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlyNet: Double = 0.0,
    val totalBillsDue: Double = 0.0,
    val currentMonth: YearMonth = YearMonth.now(),
    val daySummaries: Map<LocalDate, DayTransactionSummary> = emptyMap(),
    val isLoading: Boolean = false,
    val filterType: FilterType = FilterType.ALL,
    val showBills: Boolean = true,
    val upcomingBills: List<BillWithDate> = emptyList()
)

enum class FilterType {
    ALL, INCOME, EXPENSE
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val billReminderRepository: BillReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun setFilter(filterType: FilterType) {
        _uiState.value = _uiState.value.copy(filterType = filterType)
    }

    fun toggleShowBills() {
        _uiState.value = _uiState.value.copy(showBills = !_uiState.value.showBills)
    }

    fun setMonth(month: YearMonth) {
        _uiState.value = _uiState.value.copy(currentMonth = month)
        loadTransactionsForMonth(month)
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val expenses = expenseRepository.getAllExpenses().first()
                val incomes = incomeRepository.getAllIncome().first()
                val bills = billReminderRepository.getActiveBillReminders().first()
                val daySummaries = buildDaySummaries(expenses, incomes, bills)

                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    incomes = incomes,
                    bills = bills,
                    totalIncome = incomes.sumOf { it.amount },
                    totalExpense = expenses.sumOf { it.amount },
                    daySummaries = daySummaries,
                    monthlyIncome = incomes.sumOf { it.amount },
                    monthlyExpense = expenses.sumOf { it.amount },
                    monthlyNet = incomes.sumOf { it.amount } - expenses.sumOf { it.amount },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun loadTransactionsForMonth(month: YearMonth) {
        viewModelScope.launch {
            try {
                val startOfMonth = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfMonth = month.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                val expenses = expenseRepository.getExpensesByDateRange(startOfMonth, endOfMonth).first()
                val incomes = incomeRepository.getIncomeByDateRange(startOfMonth, endOfMonth).first()
                val bills = billReminderRepository.getActiveBillReminders().first()

                val billsByDate = buildBillsByDate(bills, month)
                val daySummaries = buildDaySummaries(expenses, incomes, bills)
                val upcomingBills = buildUpcomingBills(bills)
                val totalBillsDue = billsByDate.values.flatten().sumOf { it.amount }

                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    incomes = incomes,
                    bills = bills,
                    billsByDate = billsByDate,
                    daySummaries = daySummaries,
                    upcomingBills = upcomingBills,
                    totalBillsDue = totalBillsDue,
                    monthlyIncome = incomes.sumOf { it.amount },
                    monthlyExpense = expenses.sumOf { it.amount },
                    monthlyNet = incomes.sumOf { it.amount } - expenses.sumOf { it.amount }
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun getNextDueDate(bill: BillReminder, referenceMonth: YearMonth): Long {
        val daysInMonth = referenceMonth.lengthOfMonth()
        val billingDay = bill.billingDay.coerceAtMost(daysInMonth)

        return referenceMonth.atDay(billingDay)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun buildBillsByDate(bills: List<BillReminder>, month: YearMonth): Map<Long, List<BillReminder>> {
        val result = mutableMapOf<Long, List<BillReminder>>()
        val now = System.currentTimeMillis()

        bills.forEach { bill ->
            val dueDate = getNextDueDate(bill, month)
            if (dueDate >= now - 86400000L) {
                result[dueDate] = (result[dueDate] ?: emptyList()) + bill
            }
        }
        return result
    }

    private fun buildUpcomingBills(bills: List<BillReminder>): List<BillWithDate> {
        val now = System.currentTimeMillis()
        val currentMonth = _uiState.value.currentMonth
        val sorted = bills.mapNotNull { bill ->
            val dueDate = getNextDueDate(bill, currentMonth)
            if (dueDate >= now - 86400000L) BillWithDate(bill, dueDate) else null
        }.sortedBy { it.dueDate }
        return sorted.take(3)
    }

    private fun buildDaySummaries(
        expenses: List<Expense>,
        incomes: List<Income>,
        bills: List<BillReminder>
    ): Map<LocalDate, DayTransactionSummary> {
        val summaries = mutableMapOf<LocalDate, DayTransactionSummary>()

        expenses.forEach { expense ->
            val date = java.time.Instant.ofEpochMilli(expense.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val current = summaries[date] ?: DayTransactionSummary()
            summaries[date] = current.copy(
                totalExpense = current.totalExpense + expense.amount,
                hasExpense = true,
                transactionCount = current.transactionCount + 1
            )
        }

        incomes.forEach { income ->
            val date = java.time.Instant.ofEpochMilli(income.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val current = summaries[date] ?: DayTransactionSummary()
            summaries[date] = current.copy(
                totalIncome = current.totalIncome + income.amount,
                hasIncome = true,
                transactionCount = current.transactionCount + 1
            )
        }

        if (_uiState.value.showBills) {
            val currentMonth = _uiState.value.currentMonth
            bills.forEach { bill ->
                val dueMillis = getNextDueDate(bill, currentMonth)
                val date = java.time.Instant.ofEpochMilli(dueMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val current = summaries[date] ?: DayTransactionSummary()
                summaries[date] = current.copy(
                    transactionCount = current.transactionCount + 1,
                    billCount = current.billCount + 1
                )
            }
        }

        return summaries
    }

    fun getTransactionsForDate(date: LocalDate): List<Any> {
        val state = _uiState.value
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val filteredExpenses = when (state.filterType) {
            FilterType.ALL -> state.expenses
            FilterType.EXPENSE -> state.expenses
            FilterType.INCOME -> emptyList()
        }

        val filteredIncomes = when (state.filterType) {
            FilterType.ALL -> state.incomes
            FilterType.INCOME -> state.incomes
            FilterType.EXPENSE -> emptyList()
        }

        val expensesOnDate: List<Any> = filteredExpenses.filter { it.date in startOfDay until endOfDay }
        val incomesOnDate: List<Any> = filteredIncomes.filter { it.date in startOfDay until endOfDay }

        return (expensesOnDate + incomesOnDate).ifEmpty { emptyList() }
            .sortedByDescending { obj ->
                when (obj) {
                    is Expense -> obj.date
                    is Income -> obj.date
                    else -> 0L
                }
            }
    }

    fun getBillStatus(bill: BillReminder, dueDate: Long): BillStatus {
        val now = System.currentTimeMillis()
        val todayStart = com.rudra.savingbuddy.util.DateUtils.getStartOfDay(now)
        val dueStart = com.rudra.savingbuddy.util.DateUtils.getStartOfDay(dueDate)

        return when {
            dueStart < todayStart -> BillStatus.OVERDUE
            dueStart == todayStart -> BillStatus.DUE_TODAY
            else -> BillStatus.UPCOMING
        }
    }

    fun getDaySummary(date: LocalDate): DayTransactionSummary {
        return _uiState.value.daySummaries[date] ?: DayTransactionSummary()
    }

    fun getBillsOnDate(date: LocalDate): List<BillReminder> {
        val state = _uiState.value
        val dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return state.bills.filter { bill ->
            val dueDate = getNextDueDate(bill, state.currentMonth)
            com.rudra.savingbuddy.util.DateUtils.getStartOfDay(dueDate) == dateMillis
        }
    }
}

enum class BillStatus {
    OVERDUE, DUE_TODAY, UPCOMING
}
