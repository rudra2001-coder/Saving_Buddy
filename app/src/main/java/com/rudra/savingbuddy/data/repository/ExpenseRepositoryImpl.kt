package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.converter.toDomain
import com.rudra.savingbuddy.data.local.converter.toEntity
import com.rudra.savingbuddy.data.local.dao.CategoryTotal
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<Expense>> =
        expenseDao.getAllExpenses().map { list -> list.map { it.toDomain() } }

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

    override suspend fun insertExpense(expense: Expense): Long =
        expenseDao.insertExpense(expense.toEntity())

    override suspend fun updateExpense(expense: Expense) =
        expenseDao.updateExpense(expense.toEntity())

    override suspend fun deleteExpense(id: Long) =
        expenseDao.deleteExpenseById(id)
}