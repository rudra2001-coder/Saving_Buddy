package com.rudra.savingbuddy.ui.screens.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.BillCycle
import com.rudra.savingbuddy.domain.model.BillNotificationSettings
import com.rudra.savingbuddy.domain.model.BillReminder
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.repository.AccountRepository
import com.rudra.savingbuddy.domain.repository.BillReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BillItem(
    val id: Long,
    val name: String,
    val amount: Double,
    val billingDay: Int,
    val billingCycle: BillCycle,
    val category: String,
    val isActive: Boolean,
    val notifyDaysBefore: List<Int>,
    val isNotificationEnabled: Boolean,
    val notes: String?,
    val payFromAccountId: Long? = null,
    val paidMonths: List<String> = emptyList(),
    val lastProcessedMonth: String? = null
) {
    fun getDaysUntilDue(): Int {
        val today = Calendar.getInstance()
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val currentMonth = today.get(Calendar.MONTH)
        val currentYear = today.get(Calendar.YEAR)

        var dueDay = billingDay
        var dueMonth = currentMonth
        var dueYear = currentYear

        if (billingDay < currentDay) {
            when (billingCycle) {
                BillCycle.WEEKLY -> {
                    dueDay = billingDay + 7
                }
                BillCycle.MONTHLY -> {
                    dueMonth = currentMonth + 1
                    if (dueMonth > 11) {
                        dueMonth = 0
                        dueYear++
                    }
                }
                BillCycle.QUARTERLY -> {
                    dueMonth = currentMonth + 3
                    if (dueMonth > 11) {
                        dueMonth = dueMonth - 12
                        dueYear++
                    }
                }
                BillCycle.YEARLY -> {
                    dueYear++
                }
            }
        } else if (billingDay == currentDay) {
            return 0
        }

        val dueDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, dueYear)
            set(Calendar.MONTH, dueMonth)
            set(Calendar.DAY_OF_MONTH, dueDay)
        }

        val diffMillis = dueDate.timeInMillis - today.timeInMillis
        return (diffMillis / (24 * 60 * 60 * 1000)).toInt()
    }

    fun isDueToday(): Boolean = getDaysUntilDue() == 0

    fun isUrgent(): Boolean = getDaysUntilDue() in 0..3

    fun isPaidForCurrentMonth(): Boolean {
        val cal = Calendar.getInstance()
        val key = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        return paidMonths.contains(key)
    }

    fun getMonthKey(offset: Int = 0): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        return "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun getUnpaidMonths(count: Int): List<String> {
        val result = mutableListOf<String>()
        for (i in 0 until count) {
            val m = getMonthKey(i)
            if (!paidMonths.contains(m)) {
                result.add(m)
            }
        }
        return result
    }
}

data class BillsUiState(
    val bills: List<BillItem> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val notificationSettings: BillNotificationSettings = BillNotificationSettings(),
    val totalMonthlyAmount: Double = 0.0,
    val totalWeeklyAmount: Double = 0.0,
    val upcomingBillsCount: Int = 0,
    val showPayDialog: Boolean = false,
    val payingBill: BillItem? = null,
    val selectedAccountId: Long? = null,
    val payMonthsCount: Int = 1,
    val accounts: List<Account> = emptyList(),
    val paySuccess: Boolean? = null
)

@HiltViewModel
class BillRemindersViewModel @Inject constructor(
    private val billRepository: BillReminderRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillsUiState())
    val uiState: StateFlow<BillsUiState> = _uiState.asStateFlow()

    init {
        loadBills()
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAllAccounts().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }
    }

    private fun loadBills() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            billRepository.getAllBillReminders().collect { bills ->
                val billItems = bills.map { bill ->
                    BillItem(
                        id = bill.id,
                        name = bill.name,
                        amount = bill.amount,
                        billingDay = bill.billingDay,
                        billingCycle = bill.billingCycle,
                        category = bill.category,
                        isActive = bill.isActive,
                        notifyDaysBefore = bill.notifyDaysBefore,
                        isNotificationEnabled = bill.isNotificationEnabled,
                        notes = bill.notes,
                        payFromAccountId = bill.payFromAccountId,
                        paidMonths = bill.paidMonths,
                        lastProcessedMonth = bill.lastProcessedMonth
                    )
                }.sortedBy { it.getDaysUntilDue() }

                val monthlyTotal = billItems.filter { it.billingCycle == BillCycle.MONTHLY && it.isActive }.sumOf { it.amount }
                val weeklyTotal = billItems.filter { it.billingCycle == BillCycle.WEEKLY && it.isActive }.sumOf { it.amount }
                val upcomingCount = billItems.count { it.getDaysUntilDue() <= 7 && it.isActive }

                _uiState.update {
                    it.copy(
                        bills = billItems,
                        isLoading = false,
                        totalMonthlyAmount = monthlyTotal,
                        totalWeeklyAmount = weeklyTotal,
                        upcomingBillsCount = upcomingCount
                    )
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showSettingsDialog() {
        _uiState.update { it.copy(showSettingsDialog = true) }
    }

    fun hideSettingsDialog() {
        _uiState.update { it.copy(showSettingsDialog = false) }
    }

    fun showPayDialog(bill: BillItem) {
        val defaultAccountId = bill.payFromAccountId ?: _uiState.value.accounts.firstOrNull()?.id
        _uiState.update {
            it.copy(
                showPayDialog = true,
                payingBill = bill,
                selectedAccountId = defaultAccountId,
                payMonthsCount = 1,
                paySuccess = null
            )
        }
    }

    fun hidePayDialog() {
        _uiState.update { it.copy(showPayDialog = false, payingBill = null, paySuccess = null) }
    }

    fun selectPayAccount(accountId: Long) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
    }

    fun setPayMonthsCount(count: Int) {
        _uiState.update { it.copy(payMonthsCount = count.coerceIn(1, 12)) }
    }

    fun confirmPayBill() {
        val bill = _uiState.value.payingBill ?: return
        val accountId = _uiState.value.selectedAccountId ?: return
        val monthsCount = _uiState.value.payMonthsCount

        viewModelScope.launch {
            val unpaidMonths = bill.getUnpaidMonths(monthsCount)
            if (unpaidMonths.isEmpty()) {
                _uiState.update { it.copy(paySuccess = false) }
                return@launch
            }
            val success = billRepository.payBill(bill.id, accountId, unpaidMonths)
            _uiState.update { it.copy(paySuccess = success) }
            if (success) {
                if (bill.payFromAccountId == null) {
                    billRepository.updatePayFromAccount(bill.id, accountId)
                }
                kotlinx.coroutines.delay(1500)
                hidePayDialog()
            }
        }
    }

    fun markMonthsPaid(billId: Long, months: List<String>) {
        viewModelScope.launch {
            billRepository.updatePaidMonths(billId, months)
        }
    }

    fun markMonthsUnpaid(billId: Long, months: List<String>) {
        viewModelScope.launch {
            val bill = billRepository.getBillReminderById(billId)
            if (bill != null) {
                val remaining = bill.paidMonths.filter { it !in months }
                billRepository.updatePaidMonths(billId, remaining)
            }
        }
    }

    fun saveBill(
        name: String,
        amount: Double,
        billingDay: Int,
        billingCycle: BillCycle,
        category: String,
        notifyDaysBefore: List<Int>,
        isNotificationEnabled: Boolean,
        notes: String?
    ) {
        viewModelScope.launch {
            val bill = BillReminder(
                name = name,
                amount = amount,
                billingDay = billingDay,
                billingCycle = billingCycle,
                category = category,
                isActive = true,
                notifyDaysBefore = notifyDaysBefore,
                isNotificationEnabled = isNotificationEnabled,
                notes = notes
            )
            billRepository.insertBillReminder(bill)
            hideAddDialog()
        }
    }

    fun updateBill(bill: BillReminder) {
        viewModelScope.launch {
            billRepository.updateBillReminder(bill)
        }
    }

    fun deleteBill(id: Long) {
        viewModelScope.launch {
            billRepository.deleteBillReminder(id)
        }
    }

    fun toggleBillActive(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            billRepository.updateBillActiveStatus(id, isActive)
        }
    }

    fun toggleBillNotification(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            billRepository.updateNotificationEnabled(id, enabled)
        }
    }

    fun updateNotificationSettings(settings: BillNotificationSettings) {
        _uiState.update { it.copy(notificationSettings = settings, showSettingsDialog = false) }
    }

    fun refresh() {
        loadBills()
    }
}
