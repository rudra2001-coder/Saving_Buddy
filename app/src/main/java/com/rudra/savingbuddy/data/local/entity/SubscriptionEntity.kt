package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subscriptions",
    indices = [Index("nextBillingDate"), Index("isActive")]
)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val billingCycle: String,
    val nextBillingDate: Long,
    val category: String,
    val isActive: Boolean = true,
    val notifyDaysBefore: Int = 3,
    val notes: String? = null,
    val accountId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)