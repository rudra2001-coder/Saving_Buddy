package com.rudra.savingbuddy.data.local.dao

import androidx.room.*
import com.rudra.savingbuddy.data.local.entity.AccountEntity
import com.rudra.savingbuddy.data.local.entity.AccountBalanceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountByIdFlow(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE type = :type AND isActive = 1 ORDER BY displayOrder ASC")
    fun getAccountsByType(type: String): Flow<List<AccountEntity>>

    @Query("SELECT SUM(balance) FROM accounts WHERE isActive = 1")
    fun getTotalBalance(): Flow<Double?>
 
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = :balance, lastUpdated = :lastUpdated WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Double, lastUpdated: Long = System.currentTimeMillis())

    @Query("UPDATE accounts SET usedToday = :usedToday WHERE id = :id")
    suspend fun updateDailyUsage(id: Long, usedToday: Double)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()
}

@Dao
interface AccountBalanceHistoryDao {
    @Query("SELECT * FROM account_balance_history WHERE accountId = :accountId ORDER BY date DESC LIMIT :limit")
    fun getBalanceHistory(accountId: Long, limit: Int = 30): Flow<List<AccountBalanceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalanceHistory(history: AccountBalanceHistoryEntity)

    @Query("DELETE FROM account_balance_history WHERE accountId = :accountId")
    suspend fun deleteHistoryForAccount(accountId: Long)
}