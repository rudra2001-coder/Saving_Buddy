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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log your daily expenses"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to log expenses")
            .setContentText("Don't forget to log your expenses today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "daily_reminders"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "daily_reminder"
    }
}

object NotificationHelper {
    fun scheduleDailyReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        val workManager = WorkManager.getInstance(context)
        
        val delay = calculateDelayToTime(hour, minute)
        
        val periodicWorkRequest = androidx.work.PeriodicWorkRequestBuilder<NotificationWorker>(
            1, java.util.concurrent.TimeUnit.DAYS
        )
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            NotificationWorker.WORK_NAME,
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(NotificationWorker.WORK_NAME)
    }

    private fun calculateDelayToTime(hour: Int, minute: Int): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }

        if (target.before(now)) {
            target.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}