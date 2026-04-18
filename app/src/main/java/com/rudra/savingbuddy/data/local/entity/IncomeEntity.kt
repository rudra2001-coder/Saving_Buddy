package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val source: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)