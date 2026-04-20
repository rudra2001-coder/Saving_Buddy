package com.rudra.savingbuddy.domain.model

data class BillReminder(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val billingDay: Int,
    val billingCycle: BillCycle,
    val category: String,
    val isActive: Boolean = true,
    val notifyDaysBefore: List<Int> = listOf(3, 2, 1),
    val isNotificationEnabled: Boolean = true,
    val notes: String? = null,
    val lastNotifiedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BillCycle(val displayName: String, val daysInCycle: Int) {
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365)
}

data class BillNotificationSettings(
    val notify3DaysBefore: Boolean = true,
    val notify2DaysBefore: Boolean = true,
    val notify1DayBefore: Boolean = true,
    val notifyOnDueDate: Boolean = true,
    val defaultNotifyDays: List<Int> = listOf(3, 2, 1)
)