package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.converter.*
import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.TransferDao
import com.rudra.savingbuddy.data.local.dao.AccountBalanceHistoryDao
import com.rudra.savingbuddy.data.local.entity.AccountBalanceHistoryEntity
import com.rudra.savingbuddy.domain.model.*
import com.rudra.savingbuddy.domain.repository.AccountRepository
import com.rudra.savingbuddy.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val transferDao: TransferDao,
    private val balanceHistoryDao: AccountBalanceHistoryDao
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAccountsByType(type: AccountType): Flow<List<Account>> {
        return accountDao.getAccountsByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAccountById(id: Long): Flow<Account?> {
        return accountDao.getAccountByIdFlow(id).map { it?.toDomain() }
    }

    override fun getTotalBalance(): Flow<Double> {
        return accountDao.getTotalBalance().map { it ?: 0.0 }
    }

    override fun getNetWorth(): Flow<Double> {
        return accountDao.getTotalBalance().map { it ?: 0.0 }
    }

    override suspend fun getAccount(id: Long): Account? {
        return accountDao.getAccountById(id)?.toDomain()
    }

    override suspend fun insertAccount(account: Account): Long {
        return accountDao.insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    override suspend fun updateBalance(id: Long, newBalance: Double) {
        accountDao.updateBalance(id, newBalance)
    }

    override suspend fun addMoneyToAccount(id: Long, amount: Double, note: String?): TransferResult {
        val account = accountDao.getAccountById(id)?.toDomain()
            ?: return TransferResult(success = false, errorMessage = "Account not found")

        if (amount <= 0) {
            return TransferResult(success = false, errorMessage = "Amount must be greater than 0")
        }

        val newBalance = account.balance + amount
        accountDao.updateBalance(id, newBalance)

        balanceHistoryDao.insertBalanceHistory(
            AccountBalanceHistoryEntity(
                accountId = id,
                date = System.currentTimeMillis(),
                balance = newBalance,
                changeAmount = amount,
                changeType = "DEPOSIT"
            )
        )

        return TransferResult(
            success = true,
            toAccountNewBalance = newBalance,
            transactionId = "DEP${System.currentTimeMillis()}"
        )
    }

    override suspend fun withdrawMoneyFromAccount(id: Long, amount: Double, note: String?): TransferResult {
        val account = accountDao.getAccountById(id)?.toDomain()
            ?: return TransferResult(success = false, errorMessage = "Account not found")

        if (amount <= 0) {
            return TransferResult(success = false, errorMessage = "Amount must be greater than 0")
        }

        if (account.balance < amount) {
            return TransferResult(success = false, errorMessage = "Insufficient balance")
        }

        val newBalance = account.balance - amount
        accountDao.updateBalance(id, newBalance)

        balanceHistoryDao.insertBalanceHistory(
            AccountBalanceHistoryEntity(
                accountId = id,
                date = System.currentTimeMillis(),
                balance = newBalance,
                changeAmount = -amount,
                changeType = "WITHDRAWAL"
            )
        )

        return TransferResult(
            success = true,
            fromAccountNewBalance = newBalance,
            transactionId = "WTH${System.currentTimeMillis()}"
        )
    }

    override suspend fun deleteAccount(id: Long) {
        accountDao.deleteAccountById(id)
    }

    override suspend fun transferMoney(fromId: Long, toId: Long, amount: Double, note: String?): TransferResult {
        val fromAccount = accountDao.getAccountById(fromId)?.toDomain()
            ?: return TransferResult(success = false, errorMessage = "Source account not found")
        
        val toAccount = accountDao.getAccountById(toId)?.toDomain()
            ?: return TransferResult(success = false, errorMessage = "Destination account not found")

        // Check balance
        if (fromAccount.balance < amount) {
            return TransferResult(success = false, errorMessage = "Insufficient balance")
        }

        // Check daily limit
        val provider = AccountProvider.entries.find { it.name == fromAccount.provider }
        if (provider != null && provider.dailyTransferLimit > 0) {
            val startOfDay = getStartOfDay()
            val todaySent = transferDao.getTotalSentToday(fromId, startOfDay) ?: 0.0
            if (todaySent + amount > provider.dailyTransferLimit) {
                return TransferResult(
                    success = false,
                    errorMessage = "Daily limit exceeded (${provider.dailyTransferLimit} BDT)"
                )
            }
        }

        // Process transfer
        val newFromBalance = fromAccount.balance - amount
        val newToBalance = toAccount.balance + amount
        val fee = calculateFee(fromAccount, toAccount)

        accountDao.updateBalance(fromId, newFromBalance)
        accountDao.updateBalance(toId, newToBalance)

        // Log transfer
        val transfer = Transfer(
            fromAccountId = fromId,
            toAccountId = toId,
            amount = amount,
            fee = fee,
            note = note,
            status = TransferStatus.COMPLETED,
            reference = generateReference()
        )
        transferDao.insertTransfer(transfer.toEntity())

        // Save balance snapshot
        balanceHistoryDao.insertBalanceHistory(
            AccountBalanceHistoryEntity(
                accountId = fromId,
                date = System.currentTimeMillis(),
                balance = newFromBalance
            )
        )
        balanceHistoryDao.insertBalanceHistory(
            AccountBalanceHistoryEntity(
                accountId = toId,
                date = System.currentTimeMillis(),
                balance = newToBalance
            )
        )

        return TransferResult(
            success = true,
            fromAccountNewBalance = newFromBalance,
            toAccountNewBalance = newToBalance,
            transactionId = transfer.reference
        )
    }

    override suspend fun getTransfersForAccount(accountId: Long): List<Transfer> {
        return transferDao.getTransfersForAccount(accountId).first().map { it.toDomain() }
    }

    override suspend fun getDailyTransferTotal(accountId: Long): Double {
        val startOfDay = getStartOfDay()
        return transferDao.getTotalSentToday(accountId, startOfDay) ?: 0.0
    }

    override suspend fun getTransfersInRange(accountId: Long, startDate: Long, endDate: Long): Double {
        return transferDao.getTransfersInDateRange(accountId, startDate, endDate) ?: 0.0
    }

    override fun getBalanceHistory(accountId: Long): Flow<List<AccountBalanceHistory>> {
        return balanceHistoryDao.getBalanceHistory(accountId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveBalanceSnapshot(accountId: Long) {
        val account = accountDao.getAccountById(accountId)?.toDomain()
        account?.let {
            balanceHistoryDao.insertBalanceHistory(
                AccountBalanceHistoryEntity(
                    accountId = accountId,
                    date = System.currentTimeMillis(),
                    balance = it.balance
                )
            )
        }
    }

    private fun calculateFee(from: Account, to: Account): Double {
        // Free within app
        return 0.0
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun generateReference(): String {
        return "TRF${System.currentTimeMillis()}"
    }
}

@Singleton
class TransferRepositoryImpl @Inject constructor(
    private val transferDao: TransferDao
) : TransferRepository {

    override fun getAllTransfers(): Flow<List<Transfer>> {
        return transferDao.getAllTransfers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransfersForAccount(accountId: Long): Flow<List<Transfer>> {
        return transferDao.getTransfersForAccount(accountId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTransfer(id: Long): Transfer? {
        return null // Not implemented
    }

    override suspend fun insertTransfer(transfer: Transfer): Long {
        return transferDao.insertTransfer(transfer.toEntity())
    }

    override suspend fun updateTransfer(transfer: Transfer) {
        transferDao.updateTransfer(transfer.toEntity())
    }

    override suspend fun deleteTransfer(id: Long) {
        // Not implemented
    }
}