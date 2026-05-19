package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY purchaseDate DESC")
    fun getAllInvestments(): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE type = :type ORDER BY purchaseDate DESC")
    fun getInvestmentsByType(type: String): Flow<List<InvestmentEntity>>

    @Query("SELECT SUM(amount) FROM investments")
    fun getTotalInvested(): Flow<Double?>

    @Query("SELECT SUM(currentValue) FROM investments")
    fun getTotalCurrentValue(): Flow<Double?>

    @Query("SELECT * FROM investments WHERE id = :id")
    suspend fun getInvestmentById(id: Long): InvestmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: InvestmentEntity): Long

    @Update
    suspend fun updateInvestment(investment: InvestmentEntity)

    @Delete
    suspend fun deleteInvestment(investment: InvestmentEntity)

    @Query("DELETE FROM investments WHERE id = :id")
    suspend fun deleteInvestmentById(id: Long)

    @Query("DELETE FROM investments")
    suspend fun deleteAll()
}
