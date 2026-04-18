package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudget(): Flow<Budget?>
    suspend fun getBudgetForMonth(month: Int, year: Int): Budget?
    suspend fun setBudget(budget: Budget)
}