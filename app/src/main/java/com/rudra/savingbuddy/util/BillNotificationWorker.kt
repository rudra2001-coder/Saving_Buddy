package com.rudra.savingbuddy.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rudra.savingbuddy.MainActivity
import com.rudra.savingbuddy.R
import com.rudra.savingbuddy.domain.model.BillCycle
import com.rudra.savingbuddy.domain.repository.BillReminderRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class BillNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val billRepository: BillReminderRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        checkAndSendBillNotifications()
        return Result.success()
    }

    private suspend fun checkAndSendBillNotifications() {
        val bills = billRepository.getBillsForNotification()
        val today = Calendar.getInstance()
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val currentMonth = today.get(Calendar.MONTH)
        val currentYear = today.get(Calendar.YEAR)
        
        bills.forEach { bill ->
            val daysUntilDue = calculateDaysUntilDue(
                bill.billingDay,
                bill.billingCycle,
                currentDay,
                currentMonth,
                currentYear
            )
            
            val shouldNotify = when (daysUntilDue) {
                3 -> bill.notifyDaysBefore.contains(3)
                2 -> bill.notifyDaysBefore.contains(2)
                1 -> bill.notifyDaysBefore.contains(1)
                0 -> true
                else -> false
            }
            
            if (shouldNotify) {
                val lastNotified = bill.lastNotifiedDate ?: 0L
                val todayStart = getTodayStartMillis()
                
                if (lastNotified < todayStart) {
                    sendBillNotification(
                        billId = bill.id,
                        billName = bill.name,
                        amount = bill.amount,
                        daysUntilDue = daysUntilDue,
                        category = bill.category
                    )
                    billRepository.updateLastNotifiedDate(bill.id, System.currentTimeMillis())
                }
            }
        }
    }

    private fun calculateDaysUntilDue(
        billingDay: Int,
        billingCycle: BillCycle,
        currentDay: Int,
        currentMonth: Int,
        currentYear: Int
    ): Int {
        val today = Calendar.getInstance()
        
        if (billingDay < currentDay) {
            when (billingCycle) {
                BillCycle.WEEKLY -> {
                    val dueDate = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, billingDay + 7 - currentDay)
                    }
                    return (dueDate.timeInMillis - today.timeInMillis).toInt() / (24 * 60 * 60 * 1000)
                }
                BillCycle.MONTHLY -> {
                    val dueDate = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, billingDay)
                        add(Calendar.MONTH, 1)
                    }
                    return (dueDate.timeInMillis - today.timeInMillis).toInt() / (24 * 60 * 60 * 1000)
                }
                BillCycle.QUARTERLY -> {
                    val dueDate = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, billingDay)
                        add(Calendar.MONTH, 3)
                    }
                    return (dueDate.timeInMillis - today.timeInMillis).toInt() / (24 * 60 * 60 * 1000)
                }
                BillCycle.YEARLY -> {
                    val dueDate = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, billingDay)
                        add(Calendar.YEAR, 1)
                    }
                    return (dueDate.timeInMillis - today.timeInMillis).toInt() / (24 * 60 * 60 * 1000)
                }
            }
        } else if (billingDay == currentDay) {
            return 0
        }
        
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, billingDay)
        }
        
        return ((dueDate.timeInMillis - today.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun getTodayStartMillis(): Long {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        return today.timeInMillis
    }

    private fun sendBillNotification(
        billId: Long,
        billName: String,
        amount: Double,
        daysUntilDue: Int,
        category: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bill Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your upcoming bill payments"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "bills")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            billId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val dueText = when (daysUntilDue) {
            0 -> "Due today"
            1 -> "Due tomorrow"
            else -> "Due in $daysUntilDue days"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$billName - $dueText")
            .setContentText("${CurrencyFormatter.format(amount)} - $category")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(billId.toInt() + NOTIFICATION_ID_BASE, notification)
    }

    companion object {
        const val CHANNEL_ID = "bill_reminders"
        const val NOTIFICATION_ID_BASE = 2000
        const val WORK_NAME = "bill_reminder_check"

        fun scheduleBillReminderCheck(context: Context) {
            val workManager = WorkManager.getInstance(context)
            
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<BillNotificationWorker>(
                1, java.util.concurrent.TimeUnit.DAYS
            )
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancelBillReminderCheck(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}