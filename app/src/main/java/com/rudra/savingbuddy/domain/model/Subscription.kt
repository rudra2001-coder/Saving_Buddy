package com.rudra.savingbuddy.domain.model

data class Subscription(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val billingCycle: BillingCycle,
    val nextBillingDate: Long,
    val category: String,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BillingCycle(val displayName: String, val days: Int) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365)
}