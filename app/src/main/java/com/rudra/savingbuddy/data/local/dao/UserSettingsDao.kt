package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)

    @Query("UPDATE user_settings SET darkMode = :darkMode WHERE id = 1")
    suspend fun updateDarkMode(darkMode: Boolean)

    @Query("UPDATE user_settings SET dailyReminderEnabled = :enabled WHERE id = 1")
    suspend fun updateDailyReminder(enabled: Boolean)

    @Query("UPDATE user_settings SET dailyReminderTime = :time WHERE id = 1")
    suspend fun updateDailyReminderTime(time: String)

    @Query("UPDATE user_settings SET billReminderEnabled = :enabled WHERE id = 1")
    suspend fun updateBillReminder(enabled: Boolean)
}