package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.converter.BillReminderMapper
import com.rudra.savingbuddy.data.local.dao.BillReminderDao
import com.rudra.savingbuddy.domain.model.BillReminder
import com.rudra.savingbuddy.domain.repository.BillReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillReminderRepositoryImpl @Inject constructor(
    private val billReminderDao: BillReminderDao
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
        billReminderDao.updateNotificationEnabled(id, enabled)
    }

    override suspend fun updateLastNotifiedDate(id: Long, date: Long) {
        billReminderDao.updateLastNotifiedDate(id, date)
    }
}