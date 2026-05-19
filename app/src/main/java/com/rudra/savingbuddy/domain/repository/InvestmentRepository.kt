package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.domain.model.Investment
import kotlinx.coroutines.flow.Flow

interface InvestmentRepository {
    fun getAllInvestments(): Flow<List<Investment>>
    fun getInvestmentsByType(type: String): Flow<List<Investment>>
    fun getTotalInvested(): Flow<Double?>
    fun getTotalCurrentValue(): Flow<Double?>
    suspend fun getInvestmentById(id: Long): Investment?
    suspend fun insertInvestment(investment: Investment): Long
    suspend fun updateInvestment(investment: Investment)
    suspend fun deleteInvestment(id: Long)
}
