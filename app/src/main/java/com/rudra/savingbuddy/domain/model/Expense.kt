package com.rudra.savingbuddy.domain.model

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val category: ExpenseCategory,
    val date: Long,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ExpenseCategory(val displayName: String, val icon: String) {
    FOOD("Food", "restaurant"),
    TRANSPORT("Transport", "directions_car"),
    BILLS("Bills", "receipt"),
    SHOPPING("Shopping", "shopping_bag"),
    OTHERS("Others", "more_horiz")
}