package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.data.local.dao.CategoryTotal
import com.rudra.savingbuddy.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<Expense>>
    fun getExpensesPaginated(limit: Int, offset: Int): Flow<List<Expense>>
    fun getExpenseCount(): Flow<Int>
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>
    fun getTodayExpenses(startOfDay: Long, endOfDay: Long): Flow<List<Expense>>
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): Flow<Double?>
    suspend fun getExpenseById(id: Long): Expense?
    fun getExpensesByCategoryGrouped(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>
    suspend fun insertExpense(expense: Expense, deductFromAccount: Boolean = true): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: Long)
}