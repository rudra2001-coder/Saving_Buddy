package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.converter.toDomain
import com.rudra.savingbuddy.data.local.converter.toEntity
import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.data.local.entity.AccountEntity
import com.rudra.savingbuddy.domain.model.AccountType
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao,
    private val accountDao: AccountDao
) : IncomeRepository {

    override fun getAllIncome(): Flow<List<Income>> =
        incomeDao.getAllIncome().map { list -> list.map { it.toDomain() } }

    override fun getIncomePaginated(limit: Int, offset: Int): Flow<List<Income>> =
        incomeDao.getIncomePaginated(limit, offset).map { list -> list.map { it.toDomain() } }

    override fun getIncomeCount(): Flow<Int> =
        incomeDao.getIncomeCount()

    override fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<Income>> =
        incomeDao.getIncomeByDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override fun getTodayIncome(startOfDay: Long, endOfDay: Long): Flow<List<Income>> =
        incomeDao.getTodayIncome(startOfDay, endOfDay).map { list -> list.map { it.toDomain() } }

    override fun getTotalIncomeByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        incomeDao.getTotalIncomeByDateRange(startDate, endDate)

    override suspend fun getIncomeById(id: Long): Income? =
        incomeDao.getIncomeById(id)?.toDomain()

    override suspend fun insertIncome(income: Income, addToSelectedAccount: Boolean): Long {
        val targetAccountId = if (addToSelectedAccount && income.accountId != null) {
            income.accountId
        } else {
            var walletAccountId = getWalletAccountId()
            if (walletAccountId == null) {
                walletAccountId = createDefaultWallet()
            }
            walletAccountId
        }
        
        if (targetAccountId != null) {
            val targetAccount = accountDao.getAccountById(targetAccountId)
            val newBalance = (targetAccount?.balance ?: 0.0) + income.amount
            accountDao.updateBalance(targetAccountId, newBalance)
            
            val incomeWithAccount = income.copy(accountId = targetAccountId)
            return incomeDao.insertIncome(incomeWithAccount.toEntity())
        }
        
        return incomeDao.insertIncome(income.toEntity())
    }

    override suspend fun updateIncome(income: Income) =
        incomeDao.updateIncome(income.toEntity())

    override suspend fun deleteIncome(id: Long) =
        incomeDao.deleteIncomeById(id)

    override suspend fun getWalletAccountId(): Long? {
        val accounts = accountDao.getAccountsByType(AccountType.WALLET.name).first()
        return accounts.firstOrNull()?.id
    }

    override suspend fun getMainBalance(): Flow<Double> {
        val walletAccountId = getWalletAccountId()
        return if (walletAccountId != null) {
            accountDao.getAccountByIdFlow(walletAccountId).map { it?.balance ?: 0.0 }
        } else {
            accountDao.getTotalBalance().map { it ?: 0.0 }
        }
    }
    
    private suspend fun createDefaultWallet(): Long {
        val wallet = AccountEntity(
            name = "Wallet",
            type = AccountType.WALLET.name,
            provider = "CASH",
            accountNumber = "WALLET",
            balance = 0.0,
            initialBalance = 0.0,
            currency = "BDT",
            iconColor = 0xFF4CAF50,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        return accountDao.insertAccount(wallet)
    }
}