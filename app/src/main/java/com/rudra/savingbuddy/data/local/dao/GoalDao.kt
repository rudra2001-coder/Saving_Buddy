package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY deadline ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY deadline ASC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCompleted = 1 ORDER BY deadline DESC")
    fun getCompletedGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Query("SELECT SUM(currentAmount) FROM goals WHERE isCompleted = 0")
    fun getTotalSavedForActiveGoals(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("UPDATE goals SET currentAmount = currentAmount + :amount WHERE id = :goalId")
    suspend fun addToGoal(goalId: Long, amount: Double)

    @Query("UPDATE goals SET isCompleted = 1 WHERE id = :goalId")
    suspend fun markGoalComplete(goalId: Long)
}