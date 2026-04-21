package com.rudra.savingbuddy.domain.model

data class UserSettings(
    val id: Int = 0,
    val darkMode: Boolean = false,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: String = "20:00",
    val billReminderEnabled: Boolean = true
)