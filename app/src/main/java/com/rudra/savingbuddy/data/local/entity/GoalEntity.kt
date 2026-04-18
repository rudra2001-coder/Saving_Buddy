package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val category: String,
    val deadline: Long,
    val isCompleted: Boolean = false,
    val autoAllocate: Boolean = false,
    val allocationPercentage: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)