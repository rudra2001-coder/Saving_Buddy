package com.rudra.savingbuddy.domain.model

data class UserSettings(
    val id: Int = 0,
    val darkMode: Boolean = false,
    val currency: String = "BDT",
    val startOfWeek: String = "Saturday",
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: String = "20:00",
    val billReminderEnabled: Boolean = true,
    val budgetAlertEnabled: Boolean = true,
    val budgetAlertPercentage: Int = 80,
    val goalReminderEnabled: Boolean = true,
    val pushNotificationEnabled: Boolean = true,
    val weeklySummaryEnabled: Boolean = true,
    val goalProgressEnabled: Boolean = false,
    val privacyModeEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val defaultAccountId: Long? = null
)