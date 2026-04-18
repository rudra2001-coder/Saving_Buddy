package com.rudra.savingbuddy.domain.model

data class Budget(
    val id: Int = 1,
    val monthlyLimit: Double,
    val month: Int,
    val year: Int,
    val categoryLimits: Map<ExpenseCategory, Double> = emptyMap(),
    val enableRollover: Boolean = false,
    val alertThreshold: Int = 80
)

data class BudgetAlert(
    val id: Long = 0,
    val category: ExpenseCategory?,
    val amount: Double,
    val percentage: Int,
    val alertDate: Long = System.currentTimeMillis()
)