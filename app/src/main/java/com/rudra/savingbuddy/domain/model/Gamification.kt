package com.rudra.savingbuddy.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Long? = null
) {
    val isUnlocked: Boolean get() = unlockedAt != null
}

data class SavingsStreak(
    val consecutiveDaysUnderBudget: Int = 0,
    val longestStreak: Int = 0,
    val totalDaysUnderBudget: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class SavingsScore(
    val score: Int = 0,
    val grade: String = "N/A",
    val factors: Map<String, Int> = emptyMap()
) {
    companion object {
        fun calculate(
            savingsRate: Double,
            budgetAdherence: Double,
            goalProgress: Float,
            streakDays: Int
        ): SavingsScore {
            var totalScore = 0
            val factors = mutableMapOf<String, Int>()

            // Savings rate (40 points max)
            val savingsPoints = when {
                savingsRate >= 50 -> 40
                savingsRate >= 30 -> 30
                savingsRate >= 20 -> 20
                savingsRate >= 10 -> 10
                else -> 0
            }
            factors["Savings Rate"] = savingsPoints
            totalScore += savingsPoints

            // Budget adherence (30 points max)
            val budgetPoints = when {
                budgetAdherence >= 0.9 -> 30
                budgetAdherence >= 0.8 -> 25
                budgetAdherence >= 0.7 -> 15
                else -> 5
            }
            factors["Budget Adherence"] = budgetPoints
            totalScore += budgetPoints

            // Goal progress (20 points max)
            val goalPoints = (goalProgress * 20).toInt()
            factors["Goal Progress"] = goalPoints
            totalScore += goalPoints

            // Streak bonus (10 points max)
            val streakPoints = when {
                streakDays >= 30 -> 10
                streakDays >= 14 -> 7
                streakDays >= 7 -> 5
                streakDays >= 3 -> 2
                else -> 0
            }
            factors["Savings Streak"] = streakPoints
            totalScore += streakPoints

            val grade = when {
                totalScore >= 90 -> "A+"
                totalScore >= 80 -> "A"
                totalScore >= 70 -> "B+"
                totalScore >= 60 -> "B"
                totalScore >= 50 -> "C"
                totalScore >= 30 -> "D"
                else -> "F"
            }

            return SavingsScore(totalScore, grade, factors)
        }
    }
}

object Achievements {
    val ALL = listOf(
        Achievement("first_save", "First Saver", "Make your first savings", "savings"),
        Achievement("budget_master", "Budget Master", "Stay under budget for 7 days", "calendar"),
        Achievement("goal_getter", "Goal Getter", "Complete your first goal", "flag"),
        Achievement("consistent", "Consistent", "Log expenses for 30 consecutive days", "trending_up"),
        Achievement("saver_extreme", "Saver Extreme", "Save 50% of income", "pie_chart"),
        Achievement("early_bird", "Early Bird", "Log expenses before noon", "alarm"),
        Achievement("variety", "Variety", "Track 5+ categories", "category")
    )
}