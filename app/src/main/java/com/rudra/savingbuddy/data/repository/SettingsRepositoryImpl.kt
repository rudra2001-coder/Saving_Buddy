package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.dao.UserSettingsDao
import com.rudra.savingbuddy.data.local.entity.UserSettingsEntity
import com.rudra.savingbuddy.domain.model.UserSettings
import com.rudra.savingbuddy.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) : SettingsRepository {

    override fun getSettings(): Flow<UserSettings?> =
        userSettingsDao.getSetting("app_settings").map { entity ->
            entity?.toDomainSettings()
        }

    override suspend fun updateSettings(settings: UserSettings) {
        userSettingsDao.insertSettings(settings.toSettingsEntity())
    }
}

fun UserSettingsEntity.toDomainSettings(): UserSettings {
    val map = value.split(";").associate {
        val parts = it.split("=")
        parts[0] to parts.getOrElse(1) { "" }
    }
    return UserSettings(
        id = 0,
        darkMode = map["darkMode"]?.toBoolean() ?: false,
        currency = map["currency"] ?: "BDT",
        startOfWeek = map["startOfWeek"] ?: "Saturday",
        dailyReminderEnabled = map["dailyReminder"]?.toBoolean() ?: true,
        dailyReminderTime = map["dailyTime"] ?: "20:00",
        billReminderEnabled = map["billReminder"]?.toBoolean() ?: true,
        budgetAlertEnabled = map["budgetAlert"]?.toBoolean() ?: true,
        budgetAlertPercentage = map["budgetAlertPct"]?.toIntOrNull() ?: 80,
        goalReminderEnabled = map["goalReminder"]?.toBoolean() ?: true,
        pushNotificationEnabled = map["pushNotification"]?.toBoolean() ?: true,
        weeklySummaryEnabled = map["weeklySummary"]?.toBoolean() ?: true,
        goalProgressEnabled = map["goalProgress"]?.toBoolean() ?: false,
        privacyModeEnabled = map["privacyMode"]?.toBoolean() ?: false,
        biometricLockEnabled = map["biometricLock"]?.toBoolean() ?: false,
        defaultAccountId = map["defaultAccountId"]?.toLongOrNull()
    )
}

fun UserSettings.toSettingsEntity(): UserSettingsEntity {
    val valueStr = listOf(
        "darkMode=$darkMode",
        "currency=$currency",
        "startOfWeek=$startOfWeek",
        "dailyReminder=$dailyReminderEnabled",
        "dailyTime=$dailyReminderTime",
        "billReminder=$billReminderEnabled",
        "budgetAlert=$budgetAlertEnabled",
        "budgetAlertPct=$budgetAlertPercentage",
        "goalReminder=$goalReminderEnabled",
        "pushNotification=$pushNotificationEnabled",
        "weeklySummary=$weeklySummaryEnabled",
        "goalProgress=$goalProgressEnabled",
        "privacyMode=$privacyModeEnabled",
        "biometricLock=$biometricLockEnabled"
    ).joinToString(";")
    return UserSettingsEntity(
        key = "app_settings",
        value = valueStr,
        updatedAt = System.currentTimeMillis()
    )
}