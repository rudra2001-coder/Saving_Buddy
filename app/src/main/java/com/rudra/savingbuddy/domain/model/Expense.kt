package com.rudra.savingbuddy.domain.model

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: ExpenseCategory,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ExpenseCategory(val displayName: String, val icon: String) {
    FOOD("Food", "🍔"),
    TRANSPORT("Transport", "🚌"),
    BILLS("Bills", "💡"),
    SHOPPING("Shopping", "🛒"),
    ENTERTAINMENT("Entertainment", "🎬"),
    HEALTH("Health", "💊"),
    EDUCATION("Education", "📚"),
    GIFTS("Gifts", "🎁"),
    TRAVEL("Travel", "✈️"),
    SUBSCRIPTIONS("Subscriptions", "📱"),
    RENT("Rent", "🏠"),
    OTHERS("Others", "📦")
}