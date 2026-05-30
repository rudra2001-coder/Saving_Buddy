package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.converter.toDomain
import com.rudra.savingbuddy.data.local.converter.toEntity
import com.rudra.savingbuddy.data.local.dao.InvestmentDao
import com.rudra.savingbuddy.domain.model.Investment
import com.rudra.savingbuddy.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InvestmentRepositoryImpl @Inject constructor(
    private val investmentDao: InvestmentDao
) : InvestmentRepository {

    override fun getAllInvestments(): Flow<List<Investment>> =
        investmentDao.getAllInvestments().map { list -> list.map { it.toDomain() } }

    override fun getInvestmentsByType(type: String): Flow<List<Investment>> =
        investmentDao.getInvestmentsByType(type).map { list -> list.map { it.toDomain() } }

    override fun getTotalInvested(): Flow<Double?> =
        investmentDao.getTotalInvested()

    override fun getTotalCurrentValue(): Flow<Double?> =
        investmentDao.getTotalCurrentValue()

    override suspend fun getInvestmentById(id: Long): Investment? =
        investmentDao.getInvestmentById(id)?.toDomain()

    override suspend fun insertInvestment(investment: Investment): Long =
        investmentDao.insertInvestment(investment.toEntity())

    override suspend fun updateInvestment(investment: Investment) =
        investmentDao.updateInvestment(investment.toEntity())

    override suspend fun deleteInvestment(id: Long) =
        investmentDao.deleteInvestmentById(id)
}
