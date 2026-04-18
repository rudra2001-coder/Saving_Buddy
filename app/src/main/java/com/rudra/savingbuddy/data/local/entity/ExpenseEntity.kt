package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: Long,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)