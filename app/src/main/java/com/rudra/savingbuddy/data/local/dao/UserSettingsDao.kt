package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings")
    fun getAllSettings(): Flow<List<UserSettingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSettings(settings: List<UserSettingsEntity>)

    @Query("DELETE FROM user_settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)

    @Query("DELETE FROM user_settings")
    suspend fun deleteAllSettings()

    suspend fun getSettings(): Flow<UserSettingsEntity?> = getSetting("app_settings")
}