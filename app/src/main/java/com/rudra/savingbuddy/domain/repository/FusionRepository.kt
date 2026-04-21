package com.rudra.savingbuddy.domain.repository

import com.rudra.savingbuddy.domain.model.*
import kotlinx.coroutines.flow.Flow

interface FusionRepository {
    fun getUnifiedTransactions(limit: Int = 50): Flow<List<UnifiedTransaction>>
    
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<UnifiedTransaction>>
    
    fun getTransactionsForAccount(accountId: Long): Flow<List<UnifiedTransaction>>
    
    fun getNetWorthSummary(): Flow<NetWorthSummary>
    
    fun getAccountHealthList(): Flow<List<AccountHealth>>
    
    fun getTransferPatterns(): Flow<List<TransferPattern>>
    
    fun getFusionInsights(): Flow<List<FusionInsight>>
    
    fun getGoalFundingSuggestions(): Flow<List<GoalFundingSuggestion>>
    
    suspend fun processTransferWithFusion(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        note: String?
    ): TransferResult
    
    suspend fun allocateToGoal(goalId: Long, amount: Double, fromAccountId: Long): Boolean
}
