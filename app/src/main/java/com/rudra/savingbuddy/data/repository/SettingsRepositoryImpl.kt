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
        userSettingsDao.getSettings().map { it?.toDomain() }

    override suspend fun updateSettings(settings: UserSettings) =
        userSettingsDao.insertSettings(settings.toEntity())
}

fun UserSettingsEntity.toDomain() = UserSettings(
    id = id,
    darkMode = darkMode,
    dailyReminderEnabled = dailyReminderEnabled,
    dailyReminderTime = dailyReminderTime,
    billReminderEnabled = billReminderEnabled
)

fun UserSettings.toEntity() = UserSettingsEntity(
    id = id,
    darkMode = darkMode,
    dailyReminderEnabled = dailyReminderEnabled,
    dailyReminderTime = dailyReminderTime,
    billReminderEnabled = billReminderEnabled
)