package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget WHERE id = 1")
    fun getBudget(): Flow<BudgetEntity?>

    @Query("SELECT * FROM budget WHERE month = :month AND year = :year")
    suspend fun getBudgetForMonth(month: Int, year: Int): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Query("DELETE FROM budget WHERE id = 1")
    suspend fun deleteBudget()
}