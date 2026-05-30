package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getIncomePaginated(limit: Int, offset: Int): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT COUNT(*) FROM income")
    fun getIncomeCount(): Flow<Int>

    @Query("SELECT * FROM income WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTodayIncome(startOfDay: Long, endOfDay: Long): Flow<List<IncomeEntity>>

    @Query("SELECT SUM(amount) FROM income WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalIncomeByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT * FROM income WHERE id = :id")
    suspend fun getIncomeById(id: Long): IncomeEntity?

    @Query("SELECT * FROM income WHERE category = :category AND date BETWEEN :startDate AND :endDate")
    fun getIncomeByCategory(category: String, startDate: Long, endDate: Long): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("DELETE FROM income WHERE id = :id")
    suspend fun deleteIncomeById(id: Long)

    @Query("DELETE FROM income")
    suspend fun deleteAll()

    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>
}