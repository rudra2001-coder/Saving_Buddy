package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey
    val id: Int = 1,
    val monthlyLimit: Double,
    val month: Int,
    val year: Int
)