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
import com.rudra.savingbuddy.domain.model.BillingCycle
import com.rudra.savingbuddy.domain.repository.SubscriptionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class SubscriptionNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val subscriptionRepository: SubscriptionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        checkAndSendSubscriptionNotifications()
        return Result.success()
    }

    private suspend fun checkAndSendSubscriptionNotifications() {
        val subscriptions = subscriptionRepository.getSubscriptionsForNotification()
        val now = System.currentTimeMillis()

        subscriptions.forEach { subscription ->
            val daysUntilDue = ((subscription.nextBillingDate - now) / (24 * 60 * 60 * 1000L)).toInt()

            val shouldNotify = when (daysUntilDue) {
                3, 2, 1, 0 -> true
                else -> false
            }

            if (shouldNotify) {
                sendSubscriptionNotification(
                    subscriptionId = subscription.id,
                    subscriptionName = subscription.name,
                    amount = subscription.amount,
                    daysUntilDue = daysUntilDue,
                    category = subscription.category
                )
            }
        }
    }

    private fun sendSubscriptionNotification(
        subscriptionId: Long,
        subscriptionName: String,
        amount: Double,
        daysUntilDue: Int,
        category: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Subscription Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your upcoming subscription payments"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "budget")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            subscriptionId.toInt() + NOTIFICATION_ID_BASE,
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
            .setContentTitle("$subscriptionName - $dueText")
            .setContentText("${CurrencyFormatter.format(amount)} - $category subscription")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(subscriptionId.toInt() + NOTIFICATION_ID_BASE, notification)
    }

    companion object {
        const val CHANNEL_ID = "subscription_reminders"
        const val NOTIFICATION_ID_BASE = 3000
        const val WORK_NAME = "subscription_reminder_check"

        fun scheduleSubscriptionReminderCheck(context: Context) {
            val workManager = WorkManager.getInstance(context)

            val workRequest = androidx.work.PeriodicWorkRequestBuilder<SubscriptionNotificationWorker>(
                1, java.util.concurrent.TimeUnit.DAYS
            )
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancelSubscriptionReminderCheck(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}