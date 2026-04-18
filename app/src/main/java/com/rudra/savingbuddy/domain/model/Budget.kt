package com.rudra.savingbuddy.domain.model

data class Budget(
    val id: Int = 1,
    val monthlyLimit: Double,
    val month: Int,
    val year: Int
)