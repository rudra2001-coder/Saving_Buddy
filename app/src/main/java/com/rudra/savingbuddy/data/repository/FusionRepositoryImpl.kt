package com.rudra.savingbuddy.data.repository

import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.data.local.dao.GoalDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.data.local.dao.TransferDao
import com.rudra.savingbuddy.data.local.entity.AccountEntity
import com.rudra.savingbuddy.data.local.entity.ExpenseEntity
import com.rudra.savingbuddy.data.local.entity.GoalEntity
import com.rudra.savingbuddy.data.local.entity.IncomeEntity
import com.rudra.savingbuddy.data.local.entity.TransferEntity
import com.rudra.savingbuddy.domain.model.*
import com.rudra.savingbuddy.domain.repository.FusionRepository
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FusionRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val transferDao: TransferDao,
    private val goalDao: GoalDao
) : FusionRepository {

    override fun getUnifiedTransactions(limit: Int): Flow<List<UnifiedTransaction>> {
        return combine(
            incomeDao.getAllIncome(),
            expenseDao.getAllExpenses(),
            transferDao.getAllTransfers(),
            accountDao.getAllAccounts()
        ) { incomes, expenses, transfers, accounts ->
            val accountMap = accounts.associateBy { it.id }
            val transactions = mutableListOf<UnifiedTransaction>()
            
            incomes.forEach { income ->
                val account = accountMap[income.id]
                transactions.add(
                    UnifiedTransaction(
                        id = income.id,
                        type = TransactionType.INCOME,
                        amount = income.amount,
                        category = income.category,
                        accountId = 0,
                        accountName = "Income",
                        accountProvider = income.source,
                        note = income.notes,
                        timestamp = income.date,
                        icon = "💰",
                        color = 0xFF4CAF50
                    )
                )
            }
            
            expenses.forEach { expense ->
                transactions.add(
                    UnifiedTransaction(
                        id = expense.id,
                        type = TransactionType.EXPENSE,
                        amount = expense.amount,
                        category = expense.category,
                        accountId = 0,
                        accountName = "Expense",
                        accountProvider = "Expense",
                        note = expense.notes,
                        timestamp = expense.date,
                        icon = getCategoryIcon(expense.category),
                        color = getCategoryColor(expense.category)
                    )
                )
            }
            
            transfers.forEach { transfer ->
                val fromAccount = accountMap[transfer.fromAccountId]
                val toAccount = accountMap[transfer.toAccountId]
                
                transactions.add(
                    UnifiedTransaction(
                        id = transfer.id,
                        type = TransactionType.TRANSFER_OUT,
                        amount = transfer.amount,
                        category = "Transfer",
                        accountId = transfer.fromAccountId,
                        accountName = fromAccount?.name ?: "Unknown",
                        accountProvider = fromAccount?.provider ?: "",
                        relatedAccountId = transfer.toAccountId,
                        relatedAccountName = toAccount?.name,
                        note = transfer.note,
                        timestamp = transfer.timestamp,
                        icon = "↗️",
                        color = 0xFFFF9800
                    )
                )
                
                transactions.add(
                    UnifiedTransaction(
                        id = -transfer.id,
                        type = TransactionType.TRANSFER_IN,
                        amount = transfer.amount,
                        category = "Transfer",
                        accountId = transfer.toAccountId,
                        accountName = toAccount?.name ?: "Unknown",
                        accountProvider = toAccount?.provider ?: "",
                        relatedAccountId = transfer.fromAccountId,
                        relatedAccountName = fromAccount?.name,
                        note = transfer.note,
                        timestamp = transfer.timestamp,
                        icon = "↙️",
                        color = 0xFF2196F3
                    )
                )
            }
            
            transactions.sortedByDescending { it.timestamp }.take(limit)
        }
    }

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<UnifiedTransaction>> {
        return getUnifiedTransactions(1000).map { transactions ->
            transactions.filter { it.timestamp in startDate..endDate }
        }
    }

    override fun getTransactionsForAccount(accountId: Long): Flow<List<UnifiedTransaction>> {
        return getUnifiedTransactions(100).map { transactions ->
            transactions.filter { it.accountId == accountId || it.relatedAccountId == accountId }
        }
    }

    override fun getNetWorthSummary(): Flow<NetWorthSummary> {
        return accountDao.getAllAccounts().map { accounts ->
            val assets = accounts.filter { it.balance > 0 }
            val liabilities = accounts.filter { it.balance < 0 }
            
            val assetsByType = assets.groupBy { 
                AccountType.entries.find { type -> type.name == it.type } ?: AccountType.WALLET 
            }.mapValues { entry -> entry.value.sumOf { it.balance } }
            
            val liabilitiesByType = liabilities.groupBy { 
                AccountType.entries.find { type -> type.name == it.type } ?: AccountType.WALLET 
            }.mapValues { entry -> -entry.value.sumOf { it.balance } }
            
            NetWorthSummary(
                totalAssets = assets.sumOf { it.balance },
                totalLiabilities = liabilities.sumOf { -it.balance },
                netWorth = accounts.sumOf { it.balance },
                assetsByType = assetsByType,
                liabilitiesByType = liabilitiesByType
            )
        }
    }

    override fun getAccountHealthList(): Flow<List<AccountHealth>> {
        val startOfDay = getStartOfDay()
        
        return combine(
            accountDao.getAllAccounts(),
            transferDao.getAllTransfers()
        ) { accounts, transfers ->
            accounts.map { account ->
                val todaySent = transfers
                    .filter { it.fromAccountId == account.id && it.timestamp >= startOfDay }
                    .sumOf { it.amount }
                
                val status = calculateHealthStatus(account, todaySent)
                val recommendation = generateHealthRecommendation(account, todaySent, status)
                
                AccountHealth(
                    accountId = account.id,
                    accountName = account.name,
                    provider = account.provider,
                    balance = account.balance,
                    dailyLimit = account.dailyLimit,
                    usedToday = todaySent,
                    status = status,
                    recommendation = recommendation
                )
            }
        }
    }

    override fun getTransferPatterns(): Flow<List<TransferPattern>> {
        return transferDao.getAllTransfers().combine(accountDao.getAllAccounts()) { transfers, accounts ->
            val accountMap = accounts.associateBy { it.id }
            
            transfers.groupBy { "${it.fromAccountId}_${it.toAccountId}" }
                .map { (key, group) ->
                    val fromId = group.first().fromAccountId
                    val toId = group.first().toAccountId
                    val fromAccount = accountMap[fromId]
                    val toAccount = accountMap[toId]
                    
                    TransferPattern(
                        fromAccountId = fromId,
                        fromAccountName = fromAccount?.name ?: "Unknown",
                        toAccountId = toId,
                        toAccountName = toAccount?.name ?: "Unknown",
                        totalTransferred = group.sumOf { it.amount },
                        transactionCount = group.size,
                        averageAmount = group.sumOf { it.amount } / group.size,
                        lastTransferDate = group.maxOf { it.timestamp }
                    )
                }
                .sortedByDescending { it.transactionCount }
        }
    }

    override fun getFusionInsights(): Flow<List<FusionInsight>> {
        return combine(
            accountDao.getAllAccounts(),
            incomeDao.getAllIncome(),
            expenseDao.getAllExpenses(),
            transferDao.getAllTransfers(),
            goalDao.getAllGoals()
        ) { accounts, incomes, expenses, transfers, goals ->
            val insights = mutableListOf<FusionInsight>()
            
            val startOfMonth = getStartOfMonth()
            val startOfWeek = getStartOfWeek()
            val startOfDay = getStartOfDay()
            
            val monthExpenses = expenses.filter { it.date >= startOfMonth }.sumOf { it.amount }
            val monthIncome = incomes.filter { it.date >= startOfMonth }.sumOf { it.amount }
            val weekExpenses = expenses.filter { it.date >= startOfWeek }.sumOf { it.amount }
            
            // 1. SPENDING WARNING - High spending ratio
            if (monthIncome > 0 && monthExpenses > monthIncome * 0.9) {
                insights.add(
                    FusionInsight(
                        type = InsightType.SPENDING_WARNING,
                        title = "High Spending Alert",
                        description = "You've spent ${((monthExpenses / monthIncome) * 100).toInt()}% of your monthly income. Consider reducing expenses.",
                        value = monthExpenses
                    )
                )
            }
            
            // 2. TRANSFER HABIT - Multiple transfers today
            val todayTransfers = transfers.filter { it.timestamp >= startOfDay }
            if (todayTransfers.size >= 5) {
                val totalTransferredToday = todayTransfers.sumOf { it.amount }
                insights.add(
                    FusionInsight(
                        type = InsightType.TRANSFER_HABIT,
                        title = "Multiple Transfers Today",
                        description = "You've made ${todayTransfers.size} transfers worth ৳${String.format("%.0f", totalTransferredToday)} today. This might indicate unnecessary transactions.",
                        value = totalTransferredToday
                    )
                )
            }
            
            // 3. DAILY LIMIT WARNING - bKash/Nagad style
            accounts.forEach { account ->
                val provider = AccountProvider.entries.find { it.name == account.provider }
                if (provider != null && provider.dailyTransferLimit > 0) {
                    val todaySent = transfers
                        .filter { it.fromAccountId == account.id && it.timestamp >= startOfDay }
                        .sumOf { it.amount }
                    val limitUsage = (todaySent / provider.dailyTransferLimit) * 100
                    
                    if (limitUsage >= 80) {
                        insights.add(
                            FusionInsight(
                                type = InsightType.BALANCE_ALERT,
                                title = "${provider.displayName} Daily Limit Warning",
                                description = "You've used ${limitUsage.toInt()}% of your daily limit (৳${String.format("%.0f", todaySent)}/৳${provider.dailyTransferLimit.toInt()}). Remaining: ৳${String.format("%.0f", provider.dailyTransferLimit - todaySent)}",
                                value = provider.dailyTransferLimit - todaySent
                            )
                        )
                    }
                }
            }
            
            // 4. LOW BALANCE ALERTS - Bangladesh MFS realism
            val lowBalanceThresholds = mapOf(
                "BKASH" to 300.0,
                "NAGAD" to 300.0,
                "ROCKET" to 300.0,
                "UPAY" to 200.0,
                "DBBL" to 1000.0,
                "CITY_BANK" to 1000.0,
                "BRAC_BANK" to 1000.0
            )
            
            accounts.filter { it.isActive }.forEach { account ->
                val threshold = lowBalanceThresholds[account.provider] ?: 500.0
                if (account.balance < threshold) {
                    val providerName = AccountProvider.entries.find { it.name == account.provider }?.displayName ?: account.provider
                    insights.add(
                        FusionInsight(
                            type = InsightType.BALANCE_ALERT,
                            title = "Low $providerName Balance",
                            description = "Your $providerName balance (৳${String.format("%.0f", account.balance)}) is low. You may not be able to pay bills or make purchases.",
                            value = account.balance,
                            actionLabel = "Top Up"
                        )
                    )
                }
            }
            
            // 5. WEEKLY SPENDING TREND
            if (weekExpenses > 5000) {
                insights.add(
                    FusionInsight(
                        type = InsightType.TREND_INFO,
                        title = "High Weekly Spending",
                        description = "You've spent ৳${String.format("%.0f", weekExpenses)} this week. Monitor your spending to stay on budget.",
                        value = weekExpenses
                    )
                )
            }
            
            // 6. SAVING OPPORTUNITY
            if (monthIncome > 0 && monthExpenses < monthIncome * 0.7) {
                val savings = monthIncome - monthExpenses
                insights.add(
                    FusionInsight(
                        type = InsightType.SAVING_OPPORTUNITY,
                        title = "Great Saving Opportunity!",
                        description = "You have ৳${String.format("%.0f", savings)} leftover this month. Consider adding to your savings goal!",
                        value = savings,
                        actionLabel = "Save Now"
                    )
                )
            }
            
            // 7. GOAL PROGRESS
            val activeGoals = goals.filter { !it.isCompleted }
            activeGoals.forEach { goal ->
                val remaining = goal.targetAmount - goal.currentAmount
                val progress = (goal.currentAmount / goal.targetAmount) * 100
                
                if (progress >= 75 && remaining > 0) {
                    insights.add(
                        FusionInsight(
                            type = InsightType.GOAL_SUGGESTION,
                            title = "Almost There! 🎯",
                            description = "You're ${progress.toInt()}% towards your ${goal.name} goal. Just ৳${String.format("%.0f", remaining)} more to go!",
                            value = remaining,
                            actionLabel = "Complete"
                        )
                    )
                } else if (remaining > goal.targetAmount * 0.5) {
                    insights.add(
                        FusionInsight(
                            type = InsightType.GOAL_SUGGESTION,
                            title = "Goal Progress: ${goal.name}",
                            description = "You're ${progress.toInt()}% towards your goal. Need ৳${String.format("%.0f", remaining)} more.",
                            value = remaining,
                            actionLabel = "Add Funds"
                        )
                    )
                }
            }
            
            // 8. TRANSFER PATTERN INSIGHT
            if (todayTransfers.isNotEmpty()) {
                val mostUsedRoute = todayTransfers
                    .groupBy { "${it.fromAccountId}_${it.toAccountId}" }
                    .maxByOrNull { it.value.size }
                
                if (mostUsedRoute != null && mostUsedRoute.value.size >= 2) {
                    val fromAcc = accounts.find { it.id == todayTransfers.first().fromAccountId }
                    val toAcc = accounts.find { it.id == todayTransfers.first().toAccountId }
                    if (fromAcc != null && toAcc != null) {
                        insights.add(
                            FusionInsight(
                                type = InsightType.TRANSFER_HABIT,
                                title = "Frequent Transfer Route",
                                description = "You often transfer from ${fromAcc.name} to ${toAcc.name}. Consider keeping more balance in ${toAcc.name} to reduce transfers.",
                                value = mostUsedRoute.value.size.toDouble()
                            )
                        )
                    }
                }
            }
            
            insights.sortedByDescending { 
                when (it.type) {
                    InsightType.SPENDING_WARNING -> 1
                    InsightType.BALANCE_ALERT -> 2
                    InsightType.TRANSFER_HABIT -> 3
                    InsightType.GOAL_SUGGESTION -> 4
                    InsightType.SAVING_OPPORTUNITY -> 5
                    InsightType.TREND_INFO -> 6
                }
            }
        }
    }

    override fun getGoalFundingSuggestions(): Flow<List<GoalFundingSuggestion>> {
        return combine(
            goalDao.getAllGoals(),
            accountDao.getAllAccounts()
        ) { goals, accounts ->
            goals.filter { !it.isCompleted && it.currentAmount < it.targetAmount }
                .mapNotNull { goal ->
                    val accountsWithBalance = accounts.filter { it.balance > 100 }
                    if (accountsWithBalance.isEmpty()) return@mapNotNull null
                    
                    val sourceAccount = accountsWithBalance.maxByOrNull { it.balance }!!
                    val suggestedAmount = minOf(500.0, sourceAccount.balance, goal.targetAmount - goal.currentAmount)
                    
                    GoalFundingSuggestion(
                        goalId = goal.id,
                        goalName = goal.name,
                        targetAmount = goal.targetAmount,
                        currentAmount = goal.currentAmount,
                        suggestedAmount = suggestedAmount,
                        fromAccountId = sourceAccount.id,
                        fromAccountName = sourceAccount.name,
                        reason = "You have ৳${String.format("%.0f", sourceAccount.balance)} in ${sourceAccount.name}"
                    )
                }
        }
    }

    override suspend fun processTransferWithFusion(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        note: String?
    ): TransferResult {
        val fromAccount = accountDao.getAccountById(fromAccountId)?.toDomain()
            ?: return TransferResult(success = false, errorMessage = "Source account not found")
        
        val toAccount = accountDao.getAccountById(toAccountId)?.toDomain()
            ?: return TransferResult(success = false, errorMessage = "Destination account not found")

        if (fromAccount.balance < amount) {
            return TransferResult(success = false, errorMessage = "Insufficient balance")
        }

        val provider = AccountProvider.entries.find { it.name == fromAccount.provider }
        if (provider != null && provider.dailyTransferLimit > 0) {
            val startOfDay = getStartOfDay()
            val todaySent = transferDao.getTotalSentToday(fromAccountId, startOfDay) ?: 0.0
            if (todaySent + amount > provider.dailyTransferLimit) {
                return TransferResult(
                    success = false,
                    errorMessage = "Daily limit exceeded (${provider.dailyTransferLimit} BDT)"
                )
            }
        }

        val newFromBalance = fromAccount.balance - amount
        val newToBalance = toAccount.balance + amount

        accountDao.updateBalance(fromAccountId, newFromBalance)
        accountDao.updateBalance(toAccountId, newToBalance)

        val transfer = Transfer(
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amount = amount,
            fee = calculateFee(fromAccount, toAccount),
            note = note,
            status = TransferStatus.COMPLETED,
            reference = generateReference()
        )
        transferDao.insertTransfer(transfer.toEntity())

        return TransferResult(
            success = true,
            fromAccountNewBalance = newFromBalance,
            toAccountNewBalance = newToBalance,
            transactionId = transfer.reference
        )
    }

    override suspend fun allocateToGoal(goalId: Long, amount: Double, fromAccountId: Long): Boolean {
        val goal = goalDao.getGoalById(goalId) ?: return false
        val account = accountDao.getAccountById(fromAccountId) ?: return false
        
        if (account.balance < amount) return false
        
        val newGoalAmount = goal.currentAmount + amount
        goalDao.updateGoalAmount(goalId, newGoalAmount)
        
        val newBalance = account.balance - amount
        accountDao.updateBalance(fromAccountId, newBalance)
        
        return true
    }

    private fun calculateHealthStatus(account: AccountEntity, todaySent: Double): HealthStatus {
        return when {
            account.dailyLimit != null && account.dailyLimit > 0 -> {
                val usagePercent = (todaySent / account.dailyLimit) * 100
                when {
                    usagePercent >= 90 -> HealthStatus.CRITICAL
                    usagePercent >= 70 -> HealthStatus.LOW
                    usagePercent >= 50 -> HealthStatus.MEDIUM
                    else -> HealthStatus.GOOD
                }
            }
            account.balance < 100 -> HealthStatus.LOW
            account.balance < 500 -> HealthStatus.MEDIUM
            else -> HealthStatus.GOOD
        }
    }

    private fun generateHealthRecommendation(account: AccountEntity, todaySent: Double, status: HealthStatus): String? {
        return when (status) {
            HealthStatus.CRITICAL -> "Critical: You've almost reached your daily transfer limit"
            HealthStatus.LOW -> "Low balance: Consider adding funds"
            HealthStatus.MEDIUM -> "Moderate: Monitor your spending"
            HealthStatus.GOOD -> null
        }
    }

    private fun getCategoryIcon(category: String): String {
        return when (category.lowercase()) {
            "food" -> "🍔"
            "transport" -> "🚗"
            "shopping" -> "🛒"
            "entertainment" -> "🎬"
            "bills" -> "📄"
            "health" -> "🏥"
            "education" -> "📚"
            "salary" -> "💼"
            "investment" -> "📈"
            else -> "💳"
        }
    }

    private fun getCategoryColor(category: String): Long {
        return when (category.lowercase()) {
            "food" -> 0xFFE91E63
            "transport" -> 0xFF2196F3
            "shopping" -> 0xFF9C27B0
            "entertainment" -> 0xFFFF9800
            "bills" -> 0xFFF44336
            "health" -> 0xFF4CAF50
            "education" -> 0xFF3F51B5
            "salary" -> 0xFF00BCD4
            "investment" -> 0xFF8BC34A
            else -> 0xFF607D8B
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun generateReference(): String {
        return "TRF${System.currentTimeMillis()}"
    }

    private fun calculateFee(from: Account, to: Account): Double {
        return when {
            from.provider == "BKASH" && to.provider in listOf("DBBL", "CITY_BANK", "BRAC_BANK") -> 10.0
            from.provider == "NAGAD" && to.provider in listOf("DBBL", "CITY_BANK", "BRAC_BANK") -> 5.0
            else -> 0.0
        }
    }

    private fun AccountEntity.toDomain(): Account {
        return Account(
            id = id,
            name = name,
            type = AccountType.entries.find { it.name == type } ?: AccountType.WALLET,
            provider = provider,
            accountNumber = accountNumber,
            balance = balance,
            initialBalance = initialBalance,
            currency = currency,
            iconColor = iconColor,
            isActive = isActive,
            lastUpdated = lastUpdated,
            createdAt = createdAt,
            dailyLimit = dailyLimit,
            usedToday = usedToday,
            linkedGoalId = linkedGoalId,
            displayOrder = displayOrder
        )
    }

    private fun Transfer.toEntity(): TransferEntity {
        return TransferEntity(
            id = id,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amount = amount,
            fee = fee,
            note = note,
            timestamp = timestamp,
            status = status.name,
            reference = reference
        )
    }
}
