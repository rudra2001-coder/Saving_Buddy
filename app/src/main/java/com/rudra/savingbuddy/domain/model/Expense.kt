package com.rudra.savingbuddy.domain.model

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: ExpenseCategory,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val accountId: Long? = null,
    val tags: List<String> = emptyList(),
    val currency: String = "INR",
    val exchangeRate: Double = 1.0,
    val dependsOnId: Long? = null,
    val dependsOnComplete: Boolean = false,
    val status: RecurringStatus = RecurringStatus.ACTIVE,
    val endCondition: EndCondition = EndCondition.NEVER,
    val endDate: Long? = null,
    val occurrencesRemaining: Int? = null,
    val pausedUntil: Long? = null,
    val isVariableAmount: Boolean = false,
    val overrideAmount: Double? = null,
    val isNeedsApproval: Boolean = false,
    val isApproved: Boolean = true,
    val gracePeriodDays: Int = 3,
    val isLate: Boolean = false,
    val lateFee: Double = 0.0,
    val isEmergencyPaused: Boolean = false,
    val lastApprovedDate: Long? = null
)

enum class ExpenseCategory(
    val displayName: String, 
    val icon: String,
    val isEssential: Boolean = false,
    val isTaxDeductible: Boolean = false
) {
    FOOD("Food", "🍔"),
    TRANSPORT("Transport", "🚌", true),
    BILLS("Bills", "💡", true),
    SHOPPING("Shopping", "🛒"),
    ENTERTAINMENT("Entertainment", "🎬"),
    HEALTH("Health", "💊", true),
    EDUCATION("Education", "📚"),
    GIFTS("Gifts", "🎁"),
    TRAVEL("Travel", "✈️"),
    SUBSCRIPTIONS("Subscriptions", "📱"),
    RENT("Rent", "🏠", true),
    UTILITY("Utility", "⚡", true, true),
    INSURANCE("Insurance", "🛡️", true, true),
    TAX("Tax", "📋", false, true),
    EMI("EMI", "🏦", true),
    OTHERS("Others", "📦");

    val isBill: Boolean
        get() = listOf(BILLS, RENT, UTILITY, EMI, INSURANCE).contains(this)
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SKIPPED
}

data class LateFeeRule(
    val recurringExpenseId: Long,
    val feeAmount: Double,
    val feePercentage: Double,
    val graceDays: Int = 3,
    val applyAfterDays: Int = 7
)

data class NotificationSettings(
    val weeklyDigest: Boolean = true,
    val digestDay: Int = 0, // 0 = Sunday
    val digestTime: String = "09:00",
    val dayBeforeReminder: Boolean = true,
    val hourBeforeReminder: Boolean = true,
    val customTimes: List<String> = emptyList()
) {
    companion object {
        val DEFAULT = NotificationSettings()
    }
}

data class EmergencyPauseSettings(
    val isEnabled: Boolean = false,
    val pausedUntil: Long? = null,
    val excludeEssential: Boolean = true,
    val excludeIncome: Boolean = true,
    val reason: String? = null
)

data class ProjectedBalance(
    val date: Long,
    val balance: Double,
    val income: Double,
    val expenses: Double,
    val recurringIncome: Double,
    val recurringExpenses: Double
)

data class DryRunResult(
    val recurringItemId: Long,
    val monthlyImpact: Double,
    val yearlyImpact: Double,
    val projectedBalances: List<ProjectedBalance>
)