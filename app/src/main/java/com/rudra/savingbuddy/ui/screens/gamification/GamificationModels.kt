package com.rudra.savingbuddy.ui.screens.gamification

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val category: BadgeCategory,
    val requirement: String,
    val isUnlocked: Boolean = false,
    val progress: Float = 0f,
    val unlockedAt: Long? = null
)

enum class BadgeCategory(val displayName: String) {
    SAVING("Saving"),
    SPENDING("Spending"),
    STREAK("Streak"),
    INVESTMENT("Investment"),
    SOCIAL("Social"),
    MILESTONE("Milestone")
}

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLogDate: Long? = null,
    val weeklyLogs: List<Boolean> = List(7) { false },
    val monthlyLogCount: Int = 0
)

data class AchievementStats(
    val totalBadges: Int = 20,
    val unlockedBadges: Int = 0,
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 100,
    val streak: StreakData = StreakData(),
    val topCategory: String = "Saving"
)


