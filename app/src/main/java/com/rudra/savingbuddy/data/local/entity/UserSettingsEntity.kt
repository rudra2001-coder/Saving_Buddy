package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val darkMode: Boolean = false,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: String = "20:00",
    val billReminderEnabled: Boolean = true
)