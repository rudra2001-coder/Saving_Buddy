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
            entity?.toDomain()
        }

    override suspend fun updateSettings(settings: UserSettings) {
        userSettingsDao.insertSettings(settings.toEntity())
    }
}

fun UserSettingsEntity.toDomain(): UserSettings {
    val map = value.split(";").associate {
        val parts = it.split("=")
        parts[0] to parts.getOrElse(1) { "" }
    }
    return UserSettings(
        id = 0,
        darkMode = map["darkMode"]?.toBoolean() ?: false,
        dailyReminderEnabled = map["dailyReminder"]?.toBoolean() ?: true,
        dailyReminderTime = map["dailyTime"] ?: "20:00",
        billReminderEnabled = map["billReminder"]?.toBoolean() ?: true
    )
}

fun UserSettings.toEntity(): UserSettingsEntity {
    val valueStr = listOfNotNull(
        "darkMode=${darkMode}".takeIf { true },
        "dailyReminder=${dailyReminderEnabled}",
        "dailyTime=$dailyReminderTime",
        "billReminder=$billReminderEnabled"
    ).joinToString(";")
    return UserSettingsEntity(
        key = "app_settings",
        value = valueStr,
        updatedAt = System.currentTimeMillis()
    )
}