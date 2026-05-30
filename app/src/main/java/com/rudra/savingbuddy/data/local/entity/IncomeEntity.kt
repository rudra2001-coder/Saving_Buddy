package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "income",
    indices = [Index("date"), Index("category"), Index("source")]
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val source: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
    val recurringEndDate: Long? = null,
    val notes: String? = null,
    val accountId: Long? = null,
    val tags: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isApproved: Boolean = true,
    val approvedBy: String? = null,
    val approvedAt: Long? = null
)

@Entity(
    tableName = "expense",
    indices = [Index("date"), Index("category")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: Long,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
    val recurringEndDate: Long? = null,
    val notes: String? = null,
    val accountId: Long? = null,
    val paymentMethod: String? = null,
    val tags: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isApproved: Boolean = true,
    val approvedBy: String? = null,
    val receiptImagePath: String? = null
)

@Entity(
    tableName = "budgets",
    indices = [Index("category"), Index("month")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val spent: Double = 0.0,
    val month: String,
    val year: Int,
    val rollover: Boolean = false,
    val alertThreshold: Double = 0.8,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "goals",
    indices = [Index("isCompleted"), Index("deadline")]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val category: String,
    val deadline: Long,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val autoAllocate: Boolean = false,
    val allocationPercentage: Double = 0.0,
    val allocationSourceAccountId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val iconEmoji: String? = null,
    val colorHex: String? = null
)

@Entity(
    tableName = "user_settings",
    indices = [Index("key")]
)
data class UserSettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bill_reminders",
    indices = [Index("billingDay"), Index("isActive")]
)
data class BillReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String,
    val billingDay: Int,
    val billingCycle: String,
    val isActive: Boolean = true,
    val isPaid: Boolean = false,
    val lastPaidDate: Long? = null,
    val nextDueDate: Long,
    val accountId: Long? = null,
    val autoPay: Boolean = false,
    val remindDaysBefore: Int = 3,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)