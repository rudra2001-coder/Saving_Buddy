package com.rudra.savingbuddy.domain.model

data class AppNotification(
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val data: String? = null
)

enum class NotificationType {
    BUDGET_ALERT,
    BILL_REMINDER,
    SUBSCRIPTION_REMINDER,
    GOAL_COMPLETE,
    SAVINGS_STREAK,
    ACHIEVEMENT,
    RECURRING_TRANSACTION,
    SYSTEM
}

data class NotificationFilter(
    val type: NotificationType? = null,
    val isRead: Boolean? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)