package com.rudra.savingbuddy.domain.model

data class Income(
    val id: Long = 0,
    val source: String = "",
    val amount: Double,
    val category: IncomeCategory,
    val date: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val accountId: Long? = null,
    val tags: List<String> = emptyList(),
    val currency: String = "INR",
    val exchangeRate: Double = 1.0,
    val linkedExpenses: List<Long> = emptyList(),
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
    val gracePeriodDays: Int = 0,
    val isLate: Boolean = false,
    val isEmergencyPaused: Boolean = false,
    val lastApprovedDate: Long? = null
)

enum class IncomeCategory(val displayName: String) {
    SALARY("Salary"),
    FREELANCE("Freelance"),
    INVESTMENTS("Investments"),
    BUSINESS("Business"),
    GIFTS("Gifts"),
    RENTAL("Rental"),
    REFUND("Refund"),
    OTHERS("Others")
}

enum class RecurringInterval(val displayName: String, val days: Int) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    BI_WEEKLY("Bi-Weekly", 14),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365),
    LAST_DAY_OF_MONTH("Last Day", -1),
    LAST_WEEKDAY_OF_MONTH("Last Weekday", -2),
    EVERY_45_DAYS("Every 45 days", 45),
    EVERY_2_MONTHS("Every 2 months", 60);

    companion object {
        fun fromDays(days: Int): RecurringInterval = when {
            days <= 1 -> DAILY
            days <= 7 -> WEEKLY
            days <= 14 -> BI_WEEKLY
            days <= 30 -> MONTHLY
            days <= 60 -> EVERY_2_MONTHS
            days <= 90 -> QUARTERLY
            days <= 365 -> YEARLY
            else -> MONTHLY
        }
    }
}

enum class EndCondition {
    NEVER,
    AFTER_N_OCCURRENCES,
    ON_SPECIFIC_DATE
}

enum class RecurringStatus {
    ACTIVE,
    PAUSED,
    NEEDS_APPROVAL,
    SKIPPED_NEXT,
    LATE,
    EMERGENCY_PAUSED
}



data class Tag(
    val name: String,
    val color: Long = 0xFF6200EE,
    val isEssential: Boolean = false,
    val isTaxDeductible: Boolean = false
) {
    companion object {
        val ESSENTIAL = Tag("#essential", color = 0xFFE53935, isEssential = true)
        val DISCRETIONARY = Tag("#discretionary", color = 0xFF43A047)
        val TAX_DEDUCTIBLE = Tag("#tax-deductible", color = 0xFF1E88E5, isTaxDeductible = true)
        val RECURRING = Tag("#recurring", color = 0xFF9C27B0)
        
        val defaultTags = listOf(ESSENTIAL, DISCRETIONARY, TAX_DEDUCTIBLE, RECURRING)
    }
}

data class RecurringDependency(
    val id: Long = 0,
    val sourceId: Long,
    val dependentId: Long,
    val delayDays: Int = 7
)

data class SharedRecurring(
    val recurringId: Long,
    val userId: Long,
    val shareAmount: Double,
    val sharePercentage: Double,
    val isManager: Boolean = false,
    val notificationsEnabled: Boolean = true
)

data class RuleTemplate(
    val id: Long = 0,
    val name: String,
    val type: String, // "Income" or "Expense"
    val amount: Double,
    val category: String,
    val interval: RecurringInterval,
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val isCopy: Boolean = false
)