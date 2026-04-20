package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.BillReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillReminderDao {
    @Query("SELECT * FROM bill_reminders ORDER BY billingDay ASC")
    fun getAllBillReminders(): Flow<List<BillReminderEntity>>

    @Query("SELECT * FROM bill_reminders WHERE isActive = 1")
    fun getActiveBillReminders(): Flow<List<BillReminderEntity>>

    @Query("SELECT * FROM bill_reminders WHERE id = :id")
    suspend fun getBillReminderById(id: Long): BillReminderEntity?

    @Query("SELECT * FROM bill_reminders WHERE isActive = 1 AND isNotificationEnabled = 1")
    suspend fun getBillsForNotification(): List<BillReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillReminder(bill: BillReminderEntity): Long

    @Update
    suspend fun updateBillReminder(bill: BillReminderEntity)

    @Delete
    suspend fun deleteBillReminder(bill: BillReminderEntity)

    @Query("DELETE FROM bill_reminders WHERE id = :id")
    suspend fun deleteBillReminderById(id: Long)

    @Query("UPDATE bill_reminders SET isActive = :isActive WHERE id = :id")
    suspend fun updateBillActiveStatus(id: Long, isActive: Boolean)

    @Query("UPDATE bill_reminders SET isNotificationEnabled = :enabled WHERE id = :id")
    suspend fun updateNotificationEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE bill_reminders SET lastNotifiedDate = :date WHERE id = :id")
    suspend fun updateLastNotifiedDate(id: Long, date: Long)
}