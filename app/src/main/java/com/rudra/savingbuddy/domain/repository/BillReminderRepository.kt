package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.domain.model.BillReminder
import kotlinx.coroutines.flow.Flow

interface BillReminderRepository {
    fun getAllBillReminders(): Flow<List<BillReminder>>
    fun getActiveBillReminders(): Flow<List<BillReminder>>
    suspend fun getBillReminderById(id: Long): BillReminder?
    suspend fun getBillsForNotification(): List<BillReminder>
    suspend fun insertBillReminder(bill: BillReminder): Long
    suspend fun updateBillReminder(bill: BillReminder)
    suspend fun deleteBillReminder(id: Long)
    suspend fun updateBillActiveStatus(id: Long, isActive: Boolean)
    suspend fun updateNotificationEnabled(id: Long, enabled: Boolean)
    suspend fun updateLastNotifiedDate(id: Long, date: Long)
    suspend fun payBill(billId: Long, fromAccountId: Long, months: List<String>): Boolean
    suspend fun updatePaidMonths(billId: Long, months: List<String>)
    suspend fun updatePayFromAccount(billId: Long, accountId: Long?)
    suspend fun getCurrentMonthKey(): String
}
