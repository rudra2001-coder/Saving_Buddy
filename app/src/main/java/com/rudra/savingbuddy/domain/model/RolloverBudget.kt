package com.rudra.savingbuddy.domain.model

data class RolloverBudget(
    val id: Int = 1,
    val monthlyLimit: Double,
    val rolloverEnabled: Boolean = false,
    val rolloverPercentage: Double = 100.0,
    val periodType: BudgetPeriod = BudgetPeriod.MONTHLY
)

enum class BudgetPeriod(val displayName: String) {
    WEEKLY("Weekly"),
    BI_WEEKLY("Bi-Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom")
}

data class CategoryBudget(
    val category: ExpenseCategory,
    val limit: Double,
    val spent: Double = 0.0,
    val isRollover: Boolean = false
) {
    val remaining: Double get() = (limit - spent).coerceAtLeast(0.0)
    val percentageUsed: Float get() = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget: Boolean get() = spent > limit
}