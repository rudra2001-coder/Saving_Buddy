package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.converter.toDomain
import com.rudra.savingbuddy.data.local.converter.toEntity
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao
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

    override suspend fun insertIncome(income: Income): Long =
        incomeDao.insertIncome(income.toEntity())

    override suspend fun updateIncome(income: Income) =
        incomeDao.updateIncome(income.toEntity())

    override suspend fun deleteIncome(id: Long) =
        incomeDao.deleteIncomeById(id)
}