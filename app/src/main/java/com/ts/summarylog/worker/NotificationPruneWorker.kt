package com.ts.summarylog.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ts.summarylog.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NotificationPruneWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = applicationContext.getSharedPreferences("summarylog_settings", Context.MODE_PRIVATE)
        val retentionDays = prefs.getInt("retention_days", 30)

        if (retentionDays > 0) {
            val cutoff = System.currentTimeMillis() - (retentionDays.toLong() * 24 * 60 * 60 * 1000L)
            val db = AppDatabase.getDatabase(applicationContext)
            db.notificationDao().deleteOlderThan(cutoff)
        }

        Result.success()
    }

    companion object {
        private const val WORK_NAME = "notification_prune_work"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<NotificationPruneWorker>(24, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest,
            )
        }
    }
}
