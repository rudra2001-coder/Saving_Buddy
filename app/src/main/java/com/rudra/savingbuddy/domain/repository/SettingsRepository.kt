package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<UserSettings?>
    suspend fun updateSettings(settings: UserSettings)
}