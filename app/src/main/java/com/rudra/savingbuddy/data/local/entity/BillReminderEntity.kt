package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_reminders")
data class BillReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val billingDay: Int,
    val billingCycle: String,
    val category: String,
    val isActive: Boolean = true,
    val notifyDaysBefore: String,
    val isNotificationEnabled: Boolean = true,
    val notes: String?,
    val lastNotifiedDate: Long?,
    val createdAt: Long = System.currentTimeMillis()
)