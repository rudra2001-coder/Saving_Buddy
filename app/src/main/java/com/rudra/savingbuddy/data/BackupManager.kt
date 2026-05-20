package com.rudra.savingbuddy.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import com.rudra.savingbuddy.data.local.dao.AccountBalanceHistoryDao
import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.BillReminderDao
import com.rudra.savingbuddy.data.local.dao.BudgetDao
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.data.local.dao.GoalDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.data.local.dao.InvestmentDao
import com.rudra.savingbuddy.data.local.dao.SubscriptionDao
import com.rudra.savingbuddy.data.local.dao.TransferDao
import com.rudra.savingbuddy.data.local.entity.*
import com.rudra.savingbuddy.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val accountDao: AccountDao,
    private val goalDao: GoalDao,
    private val budgetDao: BudgetDao,
    private val billReminderDao: BillReminderDao,
    private val transferDao: TransferDao,
    private val balanceHistoryDao: AccountBalanceHistoryDao,
    private val subscriptionDao: SubscriptionDao,
    private val investmentDao: InvestmentDao
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun exportAllData(location: BackupLocation = BackupLocation.DOWNLOADS, customPath: String? = null): BackupResult = withContext(Dispatchers.IO) {
        try {
            val backupData = collectAllData()
            val jsonString = json.encodeToString(BackupData.serializer(), backupData)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val filename = "savings_backup_${dateFormat.format(Date(backupData.timestamp))}.json"

            val resultPath = if (location == BackupLocation.CUSTOM && customPath != null) {
                val treeUri = Uri.parse(customPath)
                val dirDocument = DocumentFile.fromTreeUri(context, treeUri)
                if (dirDocument == null || !dirDocument.exists()) {
                    return@withContext BackupResult.Error("Selected folder is not available")
                }
                val file = dirDocument.createFile("application/json", filename.replace(".json", ""))
                if (file == null) {
                    return@withContext BackupResult.Error("Could not create backup file in selected folder")
                }
                context.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                file.uri.toString()
            } else {
                val backupFile = getBackupFile(filename, location)
                backupFile.parentFile?.mkdirs()
                backupFile.writeText(jsonString)
                backupFile.absolutePath
            }

            val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_backup_time", System.currentTimeMillis()).apply()

            BackupResult.Success(resultPath)
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Backup failed")
        }
    }

    suspend fun exportAllDataToUri(directoryUri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val backupData = collectAllData()
            val jsonString = json.encodeToString(BackupData.serializer(), backupData)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val filename = "savings_backup_${dateFormat.format(Date(backupData.timestamp))}.json"

            val dirDocument = DocumentFile.fromTreeUri(context, directoryUri)
            if (dirDocument == null || !dirDocument.exists()) {
                return@withContext BackupResult.Error("Selected folder is not available")
            }
            val file = dirDocument.createFile("application/json", filename.replace(".json", ""))
            if (file == null) {
                return@withContext BackupResult.Error("Could not create backup file in selected folder")
            }
            context.contentResolver.openOutputStream(file.uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }

            val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_backup_time", System.currentTimeMillis()).apply()

            BackupResult.Success(file.uri.toString())
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Backup failed")
        }
    }

    private suspend fun collectAllData(): BackupData {
        val incomes = incomeDao.getAllIncomes().first()
        val expenses = expenseDao.getAllExpenses().first()
        val accounts = accountDao.getAllAccounts().first()
        val goals = goalDao.getAllGoals().first()
        val budgets = budgetDao.getAllBudgets().first()
        val billReminders = billReminderDao.getAllBillReminders().first()
        val transfers = transferDao.getAllTransfers().first()
        val subscriptions = subscriptionDao.getAllSubscriptions().first()
        val investments = investmentDao.getAllInvestments().first()

        return BackupData(
            version = 2,
            timestamp = System.currentTimeMillis(),
            incomeList = incomes.map { it.toIncomeBackup() },
            expenseList = expenses.map { it.toExpenseBackup() },
            accounts = accounts.map { it.toAccountBackup() },
            goals = goals.map { it.toGoalBackup() },
            budgets = budgets.map { it.toBudgetBackup() },
            billReminders = billReminders.map { it.toBillReminderBackup() },
            transactions = transfers.map { it.toTransactionBackup() },
            subscriptions = subscriptions.map { it.toSubscriptionBackup() },
            investments = investments.map { it.toInvestmentBackup() }
        )
    }

    suspend fun importAllData(backupData: BackupData, replaceExisting: Boolean = true): RestoreResult = withContext(Dispatchers.IO) {
        try {
            if (replaceExisting) {
                incomeDao.deleteAll()
                expenseDao.deleteAll()
                accountDao.deleteAll()
                goalDao.deleteAll()
                budgetDao.deleteAll()
                transferDao.deleteAll()
                subscriptionDao.getAllSubscriptions().first().forEach { subscriptionDao.deleteSubscription(it) }
                investmentDao.deleteAll()
            }

            backupData.accounts.forEach { acc ->
                accountDao.insertAccount(acc.toAccountEntity())
            }
            backupData.incomeList.forEach { inc ->
                incomeDao.insertIncome(inc.toIncomeEntity())
            }
            backupData.expenseList.forEach { exp ->
                expenseDao.insertExpense(exp.toExpenseEntity())
            }
            backupData.transactions.forEach { tx ->
                transferDao.insertTransfer(tx.toTransferEntity())
            }
            backupData.goals.forEach { goal ->
                goalDao.insertGoal(goal.toGoalEntity())
            }
            backupData.budgets.forEach { budget ->
                budgetDao.insertBudget(budget.toBudgetEntity())
            }
            backupData.billReminders.forEach { bill ->
                billReminderDao.insertBillReminder(bill.toBillReminderEntity())
            }
            backupData.subscriptions.forEach { sub ->
                subscriptionDao.insertSubscription(sub.toSubscriptionEntity())
            }
            backupData.investments.forEach { inv ->
                investmentDao.insertInvestment(inv.toInvestmentEntity())
            }
            val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("backup_enabled", backupData.settings.isEnabled)
                putString("backup_frequency", backupData.settings.frequency.name)
                backupData.settings.backupDay?.let { putString("backup_day", it.name) }
                putString("backup_location", backupData.settings.backupLocation.name)
                putLong("last_backup_time", backupData.settings.lastBackupTime)
                apply()
            }

            RestoreResult.Success(backupData)
        } catch (e: Exception) {
            RestoreResult.Error("Failed to restore: ${e.message}")
        }
    }

    suspend fun parseBackupFile(filePath: String): BackupData? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext null
            val jsonString = file.readText()
            json.decodeFromString(BackupData.serializer(), jsonString)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun parseBackupFileFromString(jsonString: String): BackupData? = withContext(Dispatchers.IO) {
        try {
            json.decodeFromString(BackupData.serializer(), jsonString)
        } catch (e: Exception) {
            null
        }
    }

    fun createShareIntent(filePath: String): Intent {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Saving Buddy Backup")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    suspend fun listBackups(): List<BackupFileInfo> = withContext(Dispatchers.IO) {
        val locations = listOf(BackupLocation.DOWNLOADS, BackupLocation.INTERNAL)
        val fileBackups = locations.flatMap { loc ->
            val dir = getBackupDirectory(loc)
            if (!dir.exists()) return@flatMap emptyList()
            dir.listFiles()?.toList() ?: emptyList()
        }
            .filter { it.name.startsWith("savings_backup_") && it.extension == "json" }
            .map { BackupFileInfo(it.name, it.absolutePath, it.length(), it.lastModified()) }

        val customUri = try {
            val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
            prefs.getString("custom_backup_path", null)
        } catch (_: Exception) { null }

        val customBackups = if (customUri != null) {
            try {
                val treeUri = Uri.parse(customUri)
                val dirDocument = DocumentFile.fromTreeUri(context, treeUri)
                dirDocument?.listFiles()?.toList().orEmpty()
                    .filter { it.name?.startsWith("savings_backup_") == true && it.name?.endsWith(".json") == true }
                    .map { docFile ->
                        BackupFileInfo(
                            name = docFile.name ?: "unknown",
                            path = docFile.uri.toString(),
                            size = docFile.length(),
                            modifiedDate = docFile.lastModified()
                        )
                    }
            } catch (_: Exception) { emptyList() }
        } else emptyList()

        (fileBackups + customBackups).sortedByDescending { it.modifiedDate }
    }

    suspend fun deleteBackup(filePath: String) = withContext(Dispatchers.IO) {
        File(filePath).delete()
    }

    suspend fun loadSettings(): BackupSettings = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        BackupSettings(
            isEnabled = prefs.getBoolean("backup_enabled", false),
            frequency = try {
                BackupFrequency.valueOf(prefs.getString("backup_frequency", "DAILY") ?: "DAILY")
            } catch (_: Exception) { BackupFrequency.DAILY },
            backupDay = try {
                prefs.getString("backup_day", null)?.let { BackupDay.valueOf(it) }
            } catch (_: Exception) { null },
            backupFormat = BackupFormat.JSON,
            backupLocation = try {
                BackupLocation.valueOf(prefs.getString("backup_location", "DOWNLOADS") ?: "DOWNLOADS")
            } catch (_: Exception) { BackupLocation.DOWNLOADS },
            customBackupPath = prefs.getString("custom_backup_path", null),
            lastBackupTime = prefs.getLong("last_backup_time", 0)
        )
    }

    suspend fun saveSettings(settings: BackupSettings) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("backup_enabled", settings.isEnabled)
            putString("backup_frequency", settings.frequency.name)
            settings.backupDay?.let { putString("backup_day", it.name) }
            putString("backup_location", settings.backupLocation.name)
            if (settings.customBackupPath != null) putString("custom_backup_path", settings.customBackupPath)
            else remove("custom_backup_path")
            putLong("last_backup_time", settings.lastBackupTime)
            apply()
        }
    }

    private fun getBackupFile(filename: String, location: BackupLocation): File {
        return File(getBackupDirectory(location), filename)
    }

    fun getBackupDirectory(location: BackupLocation): File {
        return when (location) {
            BackupLocation.INTERNAL -> File(context.filesDir, "backups")
            BackupLocation.DOWNLOADS -> {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SavingBuddy Backups")
                    .also { it.mkdirs() }
            }
            BackupLocation.CUSTOM -> File(context.filesDir, "backups")
        }
    }

    private fun IncomeEntity.toIncomeBackup() = IncomeBackup(
        id = id, source = source, amount = amount, category = category,
        date = date, isRecurring = isRecurring, recurringInterval = recurringInterval,
        recurringEndDate = recurringEndDate, notes = notes, accountId = accountId,
        tags = tags, createdAt = createdAt, updatedAt = updatedAt,
        isApproved = isApproved, approvedBy = approvedBy, approvedAt = approvedAt
    )

    private fun IncomeBackup.toIncomeEntity() = IncomeEntity(
        id = if (id == 0L) 0 else id, source = source, amount = amount,
        category = category, date = date, isRecurring = isRecurring,
        recurringInterval = recurringInterval, recurringEndDate = recurringEndDate,
        notes = notes, accountId = accountId, tags = tags, createdAt = createdAt,
        updatedAt = updatedAt, isApproved = isApproved, approvedBy = approvedBy,
        approvedAt = approvedAt
    )

    private fun ExpenseEntity.toExpenseBackup() = ExpenseBackup(
        id = id, amount = amount, category = category, date = date,
        isRecurring = isRecurring, recurringInterval = recurringInterval,
        recurringEndDate = recurringEndDate, notes = notes, accountId = accountId,
        paymentMethod = paymentMethod, tags = tags, createdAt = createdAt,
        updatedAt = updatedAt, isApproved = isApproved,
        approvedBy = approvedBy, receiptImagePath = receiptImagePath
    )

    private fun ExpenseBackup.toExpenseEntity() = ExpenseEntity(
        id = if (id == 0L) 0 else id, amount = amount, category = category,
        date = date, isRecurring = isRecurring, recurringInterval = recurringInterval,
        recurringEndDate = recurringEndDate, notes = notes, accountId = accountId,
        paymentMethod = paymentMethod, tags = tags, createdAt = createdAt,
        updatedAt = updatedAt, isApproved = isApproved,
        approvedBy = approvedBy, receiptImagePath = receiptImagePath
    )

    private fun AccountEntity.toAccountBackup() = AccountBackup(
        id = id, name = name, type = type, provider = provider,
        accountNumber = accountNumber, balance = balance,
        initialBalance = initialBalance, currency = currency,
        iconColor = iconColor, isActive = isActive, isArchived = isArchived,
        lastUpdated = lastUpdated, createdAt = createdAt,
        dailyLimit = dailyLimit, usedToday = usedToday,
        linkedGoalId = linkedGoalId, displayOrder = displayOrder,
        metadata = metadata
    )

    private fun AccountBackup.toAccountEntity() = AccountEntity(
        id = if (id == 0L) 0 else id, name = name, type = type,
        provider = provider, accountNumber = accountNumber,
        balance = balance, initialBalance = initialBalance,
        currency = currency, iconColor = iconColor, isActive = isActive,
        isArchived = isArchived, lastUpdated = lastUpdated,
        createdAt = createdAt, dailyLimit = dailyLimit,
        usedToday = usedToday, linkedGoalId = linkedGoalId,
        displayOrder = displayOrder, metadata = metadata
    )

    private fun GoalEntity.toGoalBackup() = GoalBackup(
        id = id, name = name, targetAmount = targetAmount,
        currentAmount = currentAmount, category = category,
        deadline = deadline, isCompleted = isCompleted,
        completedAt = completedAt, autoAllocate = autoAllocate,
        allocationPercentage = allocationPercentage,
        allocationSourceAccountId = allocationSourceAccountId,
        createdAt = createdAt, updatedAt = updatedAt,
        notes = notes, iconEmoji = iconEmoji, colorHex = colorHex
    )

    private fun GoalBackup.toGoalEntity() = GoalEntity(
        id = if (id == 0L) 0 else id, name = name,
        targetAmount = targetAmount, currentAmount = currentAmount,
        category = category, deadline = deadline,
        isCompleted = isCompleted, completedAt = completedAt,
        autoAllocate = autoAllocate,
        allocationPercentage = allocationPercentage,
        allocationSourceAccountId = allocationSourceAccountId,
        createdAt = createdAt, updatedAt = updatedAt,
        notes = notes, iconEmoji = iconEmoji, colorHex = colorHex
    )

    private fun BudgetEntity.toBudgetBackup() = BudgetBackup(
        id = id, category = category, monthlyLimit = monthlyLimit,
        spent = spent, month = month, year = year,
        rollover = rollover, alertThreshold = alertThreshold,
        createdAt = createdAt
    )

    private fun BudgetBackup.toBudgetEntity() = BudgetEntity(
        id = if (id == 0L) 0 else id, category = category,
        monthlyLimit = monthlyLimit, spent = spent,
        month = month, year = year, rollover = rollover,
        alertThreshold = alertThreshold, createdAt = createdAt
    )

    private fun BillReminderEntity.toBillReminderBackup() = BillReminderBackup(
        id = id, name = name, amount = amount, category = category,
        billingDay = billingDay, billingCycle = billingCycle,
        isActive = isActive, isPaid = isPaid,
        lastPaidDate = lastPaidDate, nextDueDate = nextDueDate,
        accountId = accountId, autoPay = autoPay,
        remindDaysBefore = remindDaysBefore, notes = notes,
        createdAt = createdAt
    )

    private fun BillReminderBackup.toBillReminderEntity() = BillReminderEntity(
        id = if (id == 0L) 0 else id, name = name, amount = amount,
        category = category, billingDay = billingDay,
        billingCycle = billingCycle, isActive = isActive,
        isPaid = isPaid, lastPaidDate = lastPaidDate,
        nextDueDate = nextDueDate, accountId = accountId,
        autoPay = autoPay, remindDaysBefore = remindDaysBefore,
        notes = notes, createdAt = createdAt
    )

    private fun TransferEntity.toTransactionBackup() = TransactionBackup(
        id = id, fromAccountId = fromAccountId, toAccountId = toAccountId,
        amount = amount, fee = fee, note = note,
        timestamp = timestamp, status = status,
        reference = reference, category = category
    )

    private fun TransactionBackup.toTransferEntity() = TransferEntity(
        id = if (id == 0L) 0 else id, fromAccountId = fromAccountId,
        toAccountId = toAccountId, amount = amount, fee = fee,
        note = note, timestamp = timestamp, status = status,
        reference = reference, category = category
    )

    private fun SubscriptionEntity.toSubscriptionBackup() = SubscriptionBackup(
        id = id, name = name, amount = amount,
        billingCycle = billingCycle, nextBillingDate = nextBillingDate,
        category = category, isActive = isActive,
        notes = notes, createdAt = createdAt
    )

    private fun SubscriptionBackup.toSubscriptionEntity() = SubscriptionEntity(
        id = if (id == 0L) 0 else id, name = name, amount = amount,
        billingCycle = billingCycle, nextBillingDate = nextBillingDate,
        category = category, isActive = isActive,
        notifyDaysBefore = 3, notes = notes,
        accountId = null, createdAt = createdAt
    )

    private fun InvestmentEntity.toInvestmentBackup() = InvestmentBackup(
        id = id.toInt(), name = name, type = type,
        amount = amount, returns = currentValue - amount,
        returnPercentage = if (amount > 0) ((currentValue - amount) / amount * 100) else 0.0,
        notes = notes
    )

    private fun InvestmentBackup.toInvestmentEntity() = InvestmentEntity(
        id = if (id == 0) 0 else id.toLong(), name = name,
        type = type, amount = amount,
        currentValue = amount + returns,
        purchaseDate = System.currentTimeMillis(),
        notes = notes, createdAt = System.currentTimeMillis()
    )
}

sealed class BackupResult {
    data class Success(val path: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(val data: BackupData) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

data class BackupFileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val modifiedDate: Long
)
