package com.rudra.savingbuddy.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rudra.savingbuddy.data.models.BackupFrequency
import com.rudra.savingbuddy.data.models.BackupLocation
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupManager: BackupManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val settings = backupManager.loadSettings()
            val result = backupManager.exportAllData(
                location = settings.backupLocation,
                customPath = if (settings.backupLocation == BackupLocation.CUSTOM) settings.customBackupPath else null
            )
            when (result) {
                is BackupResult.Success -> Result.success()
                is BackupResult.Error -> Result.retry()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "auto_backup"

        fun schedule(context: Context, frequency: BackupFrequency) {
            val interval = when (frequency) {
                BackupFrequency.DAILY -> 1L
                BackupFrequency.WEEKLY -> 7L
                BackupFrequency.MONTHLY -> 30L
            }

            val request = PeriodicWorkRequestBuilder<BackupWorker>(interval, TimeUnit.DAYS)
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
