package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE fromAccountId = :accountId OR toAccountId = :accountId ORDER BY timestamp DESC")
    fun getTransfersForAccount(accountId: Long): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE fromAccountId = :fromId AND toAccountId = :toId AND timestamp >= :startOfDay")
    suspend fun getTransfersBetweenAccountsToday(fromId: Long, toId: Long, startOfDay: Long): List<TransferEntity>

    @Query("SELECT SUM(amount) FROM transfers WHERE fromAccountId = :accountId AND timestamp >= :startOfDay")
    suspend fun getTotalSentToday(accountId: Long, startOfDay: Long): Double?

    @Query("SELECT SUM(amount) FROM transfers WHERE fromAccountId = :accountId OR toAccountId = :accountId AND timestamp >= :startDate AND timestamp < :endDate")
    suspend fun getTransfersInDateRange(accountId: Long, startDate: Long, endDate: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity): Long

    @Update
    suspend fun updateTransfer(transfer: TransferEntity)

    @Delete
    suspend fun deleteTransfer(transfer: TransferEntity)

    @Query("DELETE FROM transfers")
    suspend fun deleteAll()
}