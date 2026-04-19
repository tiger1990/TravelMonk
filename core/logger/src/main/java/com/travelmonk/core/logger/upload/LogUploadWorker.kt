package com.travelmonk.core.logger.upload

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.travelmonk.core.logger.LogFileManager
import com.travelmonk.core.logger.TravelMonkLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that uploads all pending log files every 12 hours.
 * Runs on any network (not WiFi-only) to maximise delivery reliability.
 *
 * Files that fail to upload remain in pending/ and are retried on the next run.
 */
internal class LogUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val fileManager = LogFileManager(applicationContext)
        val sender = TravelMonkLogger.remoteSender ?: DummyHttpSender()
        LogUploadOrchestrator(
            fileManager = fileManager,
            sender = sender,
            scope = CoroutineScope(Dispatchers.IO)
        ).uploadAllPending()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "travelmonk_log_upload_periodic"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<LogUploadWorker>(12, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
