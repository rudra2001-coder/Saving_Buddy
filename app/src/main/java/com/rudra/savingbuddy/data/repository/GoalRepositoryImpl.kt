package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.dao.GoalDao
import com.rudra.savingbuddy.data.local.entity.GoalEntity
import com.rudra.savingbuddy.domain.model.Goal
import com.rudra.savingbuddy.domain.model.GoalCategory
import com.rudra.savingbuddy.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> =
        goalDao.getAllGoals().map { list -> list.map { it.toDomain() } }

    override fun getActiveGoals(): Flow<List<Goal>> =
        goalDao.getActiveGoals().map { list -> list.map { it.toDomain() } }

    override fun getCompletedGoals(): Flow<List<Goal>> =
        goalDao.getCompletedGoals().map { list -> list.map { it.toDomain() } }

    override suspend fun getGoalById(id: Long): Goal? =
        goalDao.getGoalById(id)?.toDomain()

    override suspend fun insertGoal(goal: Goal): Long =
        goalDao.insertGoal(goal.toEntity())

    override suspend fun updateGoal(goal: Goal) =
        goalDao.updateGoal(goal.toEntity())

    override suspend fun deleteGoal(goal: Goal) =
        goalDao.deleteGoal(goal.toEntity())

    override suspend fun addToGoal(goalId: Long, amount: Double) =
        goalDao.addToGoal(goalId, amount)

    override suspend fun markGoalComplete(goalId: Long) =
        goalDao.markGoalComplete(goalId)
}

fun GoalEntity.toDomain() = Goal(
    id = id,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    category = try { GoalCategory.valueOf(category) } catch (e: Exception) { GoalCategory.OTHERS },
    deadline = deadline,
    isCompleted = isCompleted,
    autoAllocate = autoAllocate,
    allocationPercentage = allocationPercentage,
    createdAt = createdAt
)

fun Goal.toEntity() = GoalEntity(
    id = id,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    category = category.name,
    deadline = deadline,
    isCompleted = isCompleted,
    autoAllocate = autoAllocate,
    allocationPercentage = allocationPercentage,
    createdAt = createdAt
)