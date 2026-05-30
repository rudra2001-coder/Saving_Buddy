package com.rudra.savingbuddy.domain.model

data class UnifiedTransaction(
    val id: Long,
    val type: TransactionType,
    val amount: Double,
    val category: String,
    val accountId: Long,
    val accountName: String,
    val accountProvider: String,
    val relatedAccountId: Long? = null,
    val relatedAccountName: String? = null,
    val note: String? = null,
    val timestamp: Long,
    val icon: String,
    val color: Long
)

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER_IN,
    TRANSFER_OUT
}

data class NetWorthSummary(
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double,
    val assetsByType: Map<AccountType, Double>,
    val liabilitiesByType: Map<AccountType, Double>
)

data class AccountHealth(
    val accountId: Long,
    val accountName: String,
    val provider: String,
    val balance: Double,
    val dailyLimit: Double?,
    val usedToday: Double,
    val status: HealthStatus,
    val recommendation: String?
)

enum class HealthStatus {
    GOOD,
    MEDIUM,
    LOW,
    CRITICAL
}

data class TransferPattern(
    val fromAccountId: Long,
    val fromAccountName: String,
    val toAccountId: Long,
    val toAccountName: String,
    val totalTransferred: Double,
    val transactionCount: Int,
    val averageAmount: Double,
    val lastTransferDate: Long
)

data class FusionInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val value: Double? = null,
    val actionLabel: String? = null,
    val actionData: Any? = null
)

enum class InsightType {
    SPENDING_WARNING,
    SAVING_OPPORTUNITY,
    TRANSFER_HABIT,
    GOAL_SUGGESTION,
    BALANCE_ALERT,
    TREND_INFO
}

data class GoalFundingSuggestion(
    val goalId: Long,
    val goalName: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val suggestedAmount: Double,
    val fromAccountId: Long,
    val fromAccountName: String,
    val reason: String
)
