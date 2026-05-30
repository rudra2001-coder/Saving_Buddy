package com.rudra.savingbuddy.util

import com.rudra.savingbuddy.domain.model.UserSettings
import com.rudra.savingbuddy.domain.repository.GoalRepository
import com.rudra.savingbuddy.domain.repository.SettingsRepository
import kotlin.math.ceil
import kotlin.math.min
import javax.inject.Inject
import javax.inject.Singleton

data class RoundUpResult(
    val roundUpAmount: Double,
    val goalName: String? = null,
    val skippedReason: String? = null
)

@Singleton
class RoundUpManager @Inject constructor(
    private val goalRepository: GoalRepository,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        private const val MIN_EXPENSE_AMOUNT = 10.0
        private const val DAILY_CAP = 50.0
    }

    fun calculateRoundUp(amount: Double, type: String, multiplier: Int): Double {
        if (amount < MIN_EXPENSE_AMOUNT) return 0.0

        val nearest = when (type) {
            "NEAREST_50" -> 50.0
            "NEAREST_100" -> 100.0
            else -> 10.0
        }

        val raw = ceil(amount / nearest) * nearest - amount
        val result = raw * multiplier
        return if (result < 0.5) 0.0 else result
    }

    suspend fun processRoundUp(
        expenseAmount: Double,
        settings: UserSettings,
        now: Long = System.currentTimeMillis()
    ): RoundUpResult {
        val roundUpAmount = calculateRoundUp(
            amount = expenseAmount,
            type = settings.roundUpType,
            multiplier = settings.roundUpMultiplier
        )
        if (roundUpAmount <= 0.0) {
            return RoundUpResult(roundUpAmount = 0.0, skippedReason = "Below minimum threshold")
        }

        val goalId = settings.roundUpGoalId ?: return RoundUpResult(
            roundUpAmount = 0.0, skippedReason = "No goal linked"
        )

        val goal = goalRepository.getGoalById(goalId)

        if (goal == null) {
            settingsRepository.updateSettings(settings.copy(roundUpEnabled = false, roundUpGoalId = null))
            return RoundUpResult(roundUpAmount = 0.0, skippedReason = "Linked goal was deleted")
        }

        if (goal.isCompleted) {
            settingsRepository.updateSettings(settings.copy(roundUpEnabled = false))
            return RoundUpResult(roundUpAmount = 0.0, skippedReason = "Linked goal is completed")
        }

        val todayStart = DateUtils.getStartOfDay(now)
        val isNewDay = settings.lastRoundUpDate != todayStart
        val dailyTotal = if (isNewDay) 0.0 else settings.dailyRoundUpTotal

        val allowed = DAILY_CAP - dailyTotal
        val actualRoundUp = min(roundUpAmount, allowed.coerceAtLeast(0.0))
        if (actualRoundUp <= 0.0) {
            return RoundUpResult(roundUpAmount = 0.0, skippedReason = "Daily cap reached")
        }

        goalRepository.addToGoal(goalId, actualRoundUp)

        val newTotal = settings.totalRoundUpSaved + actualRoundUp
        val newDailyTotal = if (isNewDay) actualRoundUp else dailyTotal + actualRoundUp
        settingsRepository.updateSettings(settings.copy(
            totalRoundUpSaved = newTotal,
            lastRoundUpDate = todayStart,
            dailyRoundUpTotal = newDailyTotal
        ))

        return RoundUpResult(
            roundUpAmount = actualRoundUp,
            goalName = goal.name
        )
    }
}
