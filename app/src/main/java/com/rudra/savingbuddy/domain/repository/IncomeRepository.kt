package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.domain.model.Income
import kotlinx.coroutines.flow.Flow

interface IncomeRepository {
    fun getAllIncome(): Flow<List<Income>>
    fun getIncomePaginated(limit: Int, offset: Int): Flow<List<Income>>
    fun getIncomeCount(): Flow<Int>
    fun getIncomeByDateRange(startDate: Long, endDate: Long): Flow<List<Income>>
    fun getTodayIncome(startOfDay: Long, endOfDay: Long): Flow<List<Income>>
    fun getTotalIncomeByDateRange(startDate: Long, endDate: Long): Flow<Double?>
    suspend fun getIncomeById(id: Long): Income?
    suspend fun insertIncome(income: Income, addToWallet: Boolean = true): Long
    suspend fun updateIncome(income: Income)
    suspend fun deleteIncome(id: Long)
    suspend fun getWalletAccountId(): Long?
    suspend fun getMainBalance(): Flow<Double>
}