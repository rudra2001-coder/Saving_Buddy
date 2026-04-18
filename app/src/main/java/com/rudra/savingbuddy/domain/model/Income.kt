package com.rudra.savingbuddy.domain.model

data class Income(
    val id: Long = 0,
    val source: String,
    val amount: Double,
    val category: IncomeCategory,
    val date: Long,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class IncomeCategory(val displayName: String) {
    SALARY("Salary"),
    FREELANCE("Freelance"),
    INVESTMENTS("Investments"),
    BUSINESS("Business"),
    GIFTS("Gifts"),
    OTHERS("Others")
}

enum class RecurringInterval(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}