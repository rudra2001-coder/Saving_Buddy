package com.rudra.savingbuddy.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.rudra.savingbuddy.R
import com.rudra.savingbuddy.data.local.dao.AccountBalanceHistoryDao
import com.rudra.savingbuddy.data.local.dao.AccountDao
import com.rudra.savingbuddy.data.local.dao.ExpenseDao
import com.rudra.savingbuddy.data.local.dao.IncomeDao
import com.rudra.savingbuddy.data.local.entity.AccountBalanceHistoryEntity
import com.rudra.savingbuddy.data.local.entity.ExpenseEntity
import com.rudra.savingbuddy.data.local.entity.IncomeEntity
import com.rudra.savingbuddy.domain.model.RecurringInterval
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val accountDao: AccountDao,
    private val balanceHistoryDao: AccountBalanceHistoryDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            processRecurringIncomes()
            processRecurringExpenses()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun processRecurringIncomes() {
        val recurringIncomes = incomeDao.getRecurringIncomes()
        val now = System.currentTimeMillis()

        for (template in recurringIncomes) {
            val intervalName = template.recurringInterval ?: continue
            val interval = try { RecurringInterval.valueOf(intervalName) } catch (e: Exception) { continue }

            val latestDate = incomeDao.getLatestOccurrenceDate(
                template.source, template.category, template.amount
            ) ?: template.date

            val nextDue = calculateNextDate(latestDate, interval)
            if (nextDue > now) continue

            if (template.recurringEndDate != null && nextDue > template.recurringEndDate) continue

            val latestCheck = incomeDao.getLatestOccurrenceDate(
                template.source, template.category, template.amount
            )
            if (latestCheck != null && latestCheck >= nextDue) continue

            val newIncome = IncomeEntity(
                source = template.source,
                amount = template.amount,
                category = template.category,
                date = nextDue,
                isRecurring = false,
                recurringInterval = null,
                notes = template.notes,
                accountId = template.accountId,
                createdAt = now,
                updatedAt = now
            )
            incomeDao.insertIncome(newIncome)

            if (template.accountId != null) {
                val account = accountDao.getAccountById(template.accountId)
                if (account != null) {
                    val newBalance = account.balance + template.amount
                    accountDao.updateBalance(template.accountId, newBalance)
                    balanceHistoryDao.insertBalanceHistory(
                        AccountBalanceHistoryEntity(
                            accountId = template.accountId,
                            date = now,
                            balance = newBalance,
                            changeAmount = template.amount,
                            changeType = "RECURRING_INCOME"
                        )
                    )
                }
            }
        }
    }

    private suspend fun processRecurringExpenses() {
        val recurringExpenses = expenseDao.getRecurringExpenses()
        val now = System.currentTimeMillis()

        for (template in recurringExpenses) {
            val intervalName = template.recurringInterval ?: continue
            val interval = try { RecurringInterval.valueOf(intervalName) } catch (e: Exception) { continue }

            val latestDate = expenseDao.getLatestOccurrenceDate(
                template.category, template.amount
            ) ?: template.date

            val nextDue = calculateNextDate(latestDate, interval)
            if (nextDue > now) continue

            if (template.recurringEndDate != null && nextDue > template.recurringEndDate) continue

            val latestCheck = expenseDao.getLatestOccurrenceDate(
                template.category, template.amount
            )
            if (latestCheck != null && latestCheck >= nextDue) continue

            val hasSufficientBalance = template.accountId == null || run {
                val account = accountDao.getAccountById(template.accountId)
                account != null && account.balance >= template.amount
            }

            if (hasSufficientBalance) {
                val newExpense = ExpenseEntity(
                    amount = template.amount,
                    category = template.category,
                    date = nextDue,
                    isRecurring = false,
                    recurringInterval = null,
                    notes = template.notes,
                    accountId = template.accountId,
                    createdAt = now,
                    updatedAt = now
                )
                expenseDao.insertExpense(newExpense)

                if (template.accountId != null) {
                    val account = accountDao.getAccountById(template.accountId)
                    if (account != null) {
                        val newBalance = account.balance - template.amount
                        accountDao.updateBalance(template.accountId, newBalance)
                        balanceHistoryDao.insertBalanceHistory(
                            AccountBalanceHistoryEntity(
                                accountId = template.accountId,
                                date = now,
                                balance = newBalance,
                                changeAmount = -template.amount,
                                changeType = "RECURRING_EXPENSE"
                            )
                        )
                    }
                }
            } else {
                val notes = if (template.notes != null) "[FAILED] ${template.notes}" else "[FAILED] Insufficient balance"
                val failedExpense = ExpenseEntity(
                    amount = template.amount,
                    category = template.category,
                    date = nextDue,
                    isRecurring = false,
                    recurringInterval = null,
                    notes = notes,
                    accountId = template.accountId,
                    createdAt = now,
                    updatedAt = now
                )
                expenseDao.insertExpense(failedExpense)
                sendFailureNotification(template.amount, template.category)
            }
        }
    }

    private fun sendFailureNotification(amount: Double, category: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "recurring_expense_failure"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recurring Expense Failures",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a recurring expense fails due to insufficient balance"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recurring Expense Failed")
            .setContentText("Insufficient balance for ৳$amount in $category")
            .setStyle(NotificationCompat.BigTextStyle().bigText("A recurring expense of ৳$amount in category \"$category\" could not be processed due to insufficient balance in the linked account."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }

    private fun calculateNextDate(lastDate: Long, interval: RecurringInterval): Long {
        val calendar = Calendar.getInstance()
        when (interval) {
            RecurringInterval.LAST_DAY_OF_MONTH -> {
                calendar.timeInMillis = lastDate
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            RecurringInterval.LAST_WEEKDAY_OF_MONTH -> {
                calendar.timeInMillis = lastDate
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, lastDay)
                while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                ) {
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                }
            }
            else -> {
                calendar.timeInMillis = lastDate
                if (interval.days > 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, interval.days)
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, 30)
                }
            }
        }
        return calendar.timeInMillis
    }

    companion object {
        const val WORK_NAME = "recurring_transaction_posting"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(1, TimeUnit.DAYS)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
