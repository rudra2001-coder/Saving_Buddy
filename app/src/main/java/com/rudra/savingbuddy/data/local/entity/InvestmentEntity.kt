package com.rudra.savingbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "investments",
    indices = [Index("type"), Index("purchaseDate")]
)
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val amount: Double,
    val currentValue: Double,
    val purchaseDate: Long,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
