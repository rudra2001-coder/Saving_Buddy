package com.rudra.savingbuddy.data.models

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val accounts: List<AccountBackup> = emptyList(),
    val transactions: List<TransactionBackup> = emptyList(),
    val goals: List<GoalBackup> = emptyList(),
    val budgets: List<BudgetBackup> = emptyList(),
    val billReminders: List<BillReminderBackup> = emptyList(),
    val incomeList: List<IncomeBackup> = emptyList(),
    val expenseList: List<ExpenseBackup> = emptyList(),
    val settings: BackupSettings = BackupSettings()
)

@Serializable
data class AccountBackup(
    val id: Long,
    val name: String,
    val type: String,
    val provider: String,
    val accountNumber: String,
    val balance: Double,
    val initialBalance: Double,
    val currency: String,
    val iconColor: Long,
    val isActive: Boolean,
    val isArchived: Boolean,
    val lastUpdated: Long,
    val createdAt: Long,
    val dailyLimit: Double?,
    val usedToday: Double,
    val linkedGoalId: Long?,
    val displayOrder: Int,
    val metadata: String?
)

@Serializable
data class TransactionBackup(
    val id: Long,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: Double,
    val fee: Double,
    val note: String?,
    val timestamp: Long,
    val status: String,
    val reference: String?,
    val category: String?
)

@Serializable
data class GoalBackup(
    val id: Long,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val category: String,
    val deadline: Long,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val autoAllocate: Boolean,
    val allocationPercentage: Double,
    val allocationSourceAccountId: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String?,
    val iconEmoji: String?,
    val colorHex: String?
)

@Serializable
data class BudgetBackup(
    val id: Long,
    val category: String,
    val monthlyLimit: Double,
    val spent: Double,
    val month: String,
    val year: Int,
    val rollover: Boolean,
    val alertThreshold: Double,
    val createdAt: Long
)

@Serializable
data class BillReminderBackup(
    val id: Long,
    val name: String,
    val amount: Double,
    val category: String,
    val billingDay: Int,
    val billingCycle: String,
    val isActive: Boolean,
    val isPaid: Boolean,
    val lastPaidDate: Long?,
    val nextDueDate: Long,
    val accountId: Long?,
    val autoPay: Boolean,
    val remindDaysBefore: Int,
    val notes: String?,
    val createdAt: Long
)

@Serializable
data class IncomeBackup(
    val id: Long,
    val source: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val isRecurring: Boolean,
    val recurringInterval: String?,
    val recurringEndDate: Long?,
    val notes: String?,
    val accountId: Long?,
    val tags: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isApproved: Boolean,
    val approvedBy: String?,
    val approvedAt: Long?
)

@Serializable
data class ExpenseBackup(
    val id: Long,
    val amount: Double,
    val category: String,
    val date: Long,
    val isRecurring: Boolean,
    val recurringInterval: String?,
    val recurringEndDate: Long?,
    val notes: String?,
    val accountId: Long?,
    val paymentMethod: String?,
    val tags: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isApproved: Boolean,
    val approvedBy: String?,
    val receiptImagePath: String?
)

@Serializable
data class BackupSettings(
    val isEnabled: Boolean = false,
    val frequency: BackupFrequency = BackupFrequency.DAILY,
    val backupDay: BackupDay? = null,
    val backupFormat: BackupFormat = BackupFormat.JSON,
    val backupLocation: BackupLocation = BackupLocation.DOWNLOADS,
    val lastBackupTime: Long = 0
)

@Serializable
enum class BackupFrequency {
    DAILY, WEEKLY, MONTHLY
}

@Serializable
enum class BackupDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

@Serializable
enum class BackupFormat {
    JSON
}

@Serializable
enum class BackupLocation {
    INTERNAL, DOWNLOADS
}