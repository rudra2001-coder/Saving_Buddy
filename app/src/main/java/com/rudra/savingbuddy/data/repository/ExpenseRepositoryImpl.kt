package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.converter.toDomain
import com.rudra.savingbuddy.data.local.converter.toEntity
import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.CategoryTotal
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val accountDao: AccountDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> =
        expenseDao.getAllExpenses().map { list -> list.map { it.toDomain() } }

    override fun getExpensesPaginated(limit: Int, offset: Int): Flow<List<Expense>> =
        expenseDao.getExpensesPaginated(limit, offset).map { list -> list.map { it.toDomain() } }

    override fun getExpenseCount(): Flow<Int> =
        expenseDao.getExpenseCount()

    override fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override fun getTodayExpenses(startOfDay: Long, endOfDay: Long): Flow<List<Expense>> =
        expenseDao.getTodayExpenses(startOfDay, endOfDay).map { list -> list.map { it.toDomain() } }

    override fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): Flow<Double?> =
        expenseDao.getTotalExpensesByDateRange(startDate, endDate)

    override suspend fun getExpenseById(id: Long): Expense? =
        expenseDao.getExpenseById(id)?.toDomain()

    override fun getExpensesByCategoryGrouped(startDate: Long, endDate: Long): Flow<List<CategoryTotal>> =
        expenseDao.getExpensesByCategoryGrouped(startDate, endDate)

    override suspend fun insertExpense(expense: Expense, deductFromAccount: Boolean): Long {
        if (deductFromAccount && expense.accountId != null) {
            val account = accountDao.getAccountById(expense.accountId)
            if (account != null && account.balance >= expense.amount) {
                val newBalance = account.balance - expense.amount
                accountDao.updateBalance(expense.accountId, newBalance)
            }
        }
        return expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun updateExpense(expense: Expense) =
        expenseDao.updateExpense(expense.toEntity())

    override suspend fun deleteExpense(id: Long) =
        expenseDao.deleteExpenseById(id)
}