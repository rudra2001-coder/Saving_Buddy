package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTodayExpenses(startOfDay: Long, endOfDay: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expense WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT * FROM expense WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expense WHERE category = :category AND date BETWEEN :startDate AND :endDate")
    fun getExpensesByCategory(category: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT category, SUM(amount) as total FROM expense WHERE date BETWEEN :startDate AND :endDate GROUP BY category")
    fun getExpensesByCategoryGrouped(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expense WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
}

data class CategoryTotal(
    val category: String,
    val total: Double
)