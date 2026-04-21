package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    fun getAccountsByType(type: AccountType): Flow<List<Account>>
    fun getAccountById(id: Long): Flow<Account?>
    fun getTotalBalance(): Flow<Double>
    fun getNetWorth(): Flow<Double>
    
    suspend fun getAccount(id: Long): Account?
    suspend fun insertAccount(account: Account): Long
    suspend fun updateAccount(account: Account)
    suspend fun updateBalance(id: Long, newBalance: Double)
    suspend fun addMoneyToAccount(id: Long, amount: Double, note: String? = null): TransferResult
    suspend fun withdrawMoneyFromAccount(id: Long, amount: Double, note: String? = null): TransferResult
    suspend fun deleteAccount(id: Long)
    
    suspend fun transferMoney(fromId: Long, toId: Long, amount: Double, note: String?): TransferResult
    suspend fun getTransfersForAccount(accountId: Long): List<Transfer>
    suspend fun getDailyTransferTotal(accountId: Long): Double
    suspend fun getTransfersInRange(accountId: Long, startDate: Long, endDate: Long): Double
    
    fun getBalanceHistory(accountId: Long): Flow<List<AccountBalanceHistory>>
    suspend fun saveBalanceSnapshot(accountId: Long)
}

interface TransferRepository {
    fun getAllTransfers(): Flow<List<Transfer>>
    fun getTransfersForAccount(accountId: Long): Flow<List<Transfer>>
    suspend fun getTransfer(id: Long): Transfer?
    suspend fun insertTransfer(transfer: Transfer): Long
    suspend fun updateTransfer(transfer: Transfer)
    suspend fun deleteTransfer(id: Long)
}