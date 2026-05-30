package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index("type"), Index("provider")]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val provider: String,
    val accountNumber: String,
    val balance: Double = 0.0,
    val initialBalance: Double = 0.0,
    val currency: String = "BDT",
    val iconColor: Long = 0xFF6200EE,
    val isActive: Boolean = true,
    val isArchived: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val dailyLimit: Double? = null,
    val usedToday: Double = 0.0,
    val linkedGoalId: Long? = null,
    val displayOrder: Int = 0,
    val metadata: String? = null
)

@Entity(
    tableName = "transfers",
    indices = [
        Index("fromAccountId"),
        Index("toAccountId"),
        Index("timestamp")
    ]
)
data class TransferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: Double,
    val fee: Double = 0.0,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED",
    val reference: String? = null,
    val category: String? = null
)

@Entity(
    tableName = "account_balance_history",
    indices = [Index("accountId"), Index("date")]
)
data class AccountBalanceHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val date: Long,
    val balance: Double,
    val changeAmount: Double? = null,
    val changeType: String? = null
)
