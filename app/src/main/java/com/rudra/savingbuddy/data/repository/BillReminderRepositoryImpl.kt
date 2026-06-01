package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.converter.BillReminderMapper
import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.BillReminderDao
import com.rudra.savingbuddy.domain.model.BillReminder
import com.rudra.savingbuddy.domain.repository.BillReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillReminderRepositoryImpl @Inject constructor(
    private val billReminderDao: BillReminderDao,
    private val accountDao: AccountDao
) : BillReminderRepository {

    override fun getAllBillReminders(): Flow<List<BillReminder>> {
        return billReminderDao.getAllBillReminders().map { entities ->
            entities.map { BillReminderMapper.toDomain(it) }
        }
    }

    override fun getActiveBillReminders(): Flow<List<BillReminder>> {
        return billReminderDao.getActiveBillReminders().map { entities ->
            entities.map { BillReminderMapper.toDomain(it) }
        }
    }

    override suspend fun getBillReminderById(id: Long): BillReminder? {
        return billReminderDao.getBillReminderById(id)?.let { BillReminderMapper.toDomain(it) }
    }

    override suspend fun getBillsForNotification(): List<BillReminder> {
        return billReminderDao.getBillsForNotification().map { BillReminderMapper.toDomain(it) }
    }

    override suspend fun insertBillReminder(bill: BillReminder): Long {
        return billReminderDao.insertBillReminder(BillReminderMapper.toEntity(bill))
    }

    override suspend fun updateBillReminder(bill: BillReminder) {
        billReminderDao.updateBillReminder(BillReminderMapper.toEntity(bill))
    }

    override suspend fun deleteBillReminder(id: Long) {
        billReminderDao.deleteBillReminderById(id)
    }

    override suspend fun updateBillActiveStatus(id: Long, isActive: Boolean) {
        billReminderDao.updateBillActiveStatus(id, isActive)
    }

    override suspend fun updateNotificationEnabled(id: Long, enabled: Boolean) {
    }

    override suspend fun updateLastNotifiedDate(id: Long, date: Long) {
    }

    override suspend fun payBill(billId: Long, fromAccountId: Long, months: List<String>): Boolean {
        val bill = billReminderDao.getBillReminderById(billId) ?: return false
        val account = accountDao.getAccountById(fromAccountId) ?: return false
        val totalAmount = bill.amount * months.size

        if (account.balance < totalAmount) return false

        val newBalance = account.balance - totalAmount
        accountDao.updateBalance(fromAccountId, newBalance)

        val existingPaid = bill.paidMonths?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        val allPaid = (existingPaid + months).distinct().sorted()
        val paidStr = if (allPaid.isEmpty()) null else allPaid.joinToString(",")
        billReminderDao.updatePaidMonths(billId, paidStr)

        val currentMonth = getCurrentMonthKey()
        billReminderDao.updateLastProcessedMonth(billId, currentMonth)

        return true
    }

    override suspend fun updatePaidMonths(billId: Long, months: List<String>) {
        val paidStr = if (months.isEmpty()) null else months.joinToString(",")
        billReminderDao.updatePaidMonths(billId, paidStr)
    }

    override suspend fun updatePayFromAccount(billId: Long, accountId: Long?) {
        billReminderDao.updatePayFromAccount(billId, accountId)
    }

    override suspend fun getCurrentMonthKey(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return "%04d-%02d".format(year, month)
    }
}
