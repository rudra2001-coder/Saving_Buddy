package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.dao.BudgetDao
import com.rudra.savingbuddy.data.local.entity.BudgetEntity
import com.rudra.savingbuddy.domain.model.Budget
import com.rudra.savingbuddy.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudget(): Flow<Budget?> =
        budgetDao.getBudget().map { it?.toDomain() }

    override suspend fun getBudgetForMonth(month: Int, year: Int): Budget? =
        budgetDao.getBudgetForMonth(month.toString(), year)?.toDomain()

    override suspend fun setBudget(budget: Budget) =
        budgetDao.insertBudget(budget.toEntity())
}

fun BudgetEntity.toDomain() = Budget(
    id = id.toInt(),
    monthlyLimit = monthlyLimit,
    month = month.toIntOrNull() ?: 1,
    year = year,
    enableRollover = rollover,
    alertThreshold = (alertThreshold * 100).toInt()
)

fun Budget.toEntity() = BudgetEntity(
    id = id.toLong(),
    category = "default",
    monthlyLimit = monthlyLimit,
    spent = 0.0,
    month = month.toString(),
    year = year,
    rollover = enableRollover,
    alertThreshold = alertThreshold / 100.0,
    createdAt = System.currentTimeMillis()
)