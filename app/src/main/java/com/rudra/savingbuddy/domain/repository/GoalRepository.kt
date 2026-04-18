package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    fun getActiveGoals(): Flow<List<Goal>>
    fun getCompletedGoals(): Flow<List<Goal>>
    suspend fun getGoalById(id: Long): Goal?
    suspend fun insertGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goal: Goal)
    suspend fun addToGoal(goalId: Long, amount: Double)
    suspend fun markGoalComplete(goalId: Long)
}