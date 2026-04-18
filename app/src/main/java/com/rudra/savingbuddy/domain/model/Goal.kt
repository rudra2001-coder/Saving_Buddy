package com.rudra.savingbuddy.domain.model

data class Goal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val category: GoalCategory,
    val deadline: Long,
    val isCompleted: Boolean = false,
    val autoAllocate: Boolean = false,
    val allocationPercentage: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    val daysRemaining: Long
        get() {
            val now = System.currentTimeMillis()
            return ((deadline - now) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
        }
}

enum class GoalCategory(val displayName: String) {
    EMERGENCY_FUND("Emergency Fund"),
    VACATION("Vacation"),
    CAR("Car"),
    HOUSE("House"),
    EDUCATION("Education"),
    ELECTRONICS("Electronics"),
    WEDDING("Wedding"),
    RETIREMENT("Retirement"),
    INVESTMENT("Investment"),
    OTHERS("Others")
}