package com.travelmonk.core.logger.upload

import com.travelmonk.core.logger.LogFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * Wires together the two upload triggers:
 * 1. Rotation-triggered: uploads whenever [LogFileManager] rotates a file into pending/.
 * 2. Periodic: [LogUploadWorker] calls [uploadAllPending] every 12 hours via WorkManager.
 *
 * On success → file deleted from pending/.
 * On failure → file stays in pending/ and is retried on the next trigger.
 */
internal class LogUploadOrchestrator(
    private val fileManager: LogFileManager,
    private val sender: RemoteLogSender,
    private val scope: CoroutineScope
) {
    fun start() {
        scope.launch {
            fileManager.rotationEvents.collect { rotatedFile ->
                uploadFile(rotatedFile)
            }
        }
    }

    suspend fun uploadAllPending() {
        fileManager.getPendingFiles().forEach { uploadFile(it) }
    }

    private suspend fun uploadFile(file: File) {
        when (sender.sendBatch(file)) {
            is UploadResult.Success -> fileManager.deleteAfterUpload(file)
            is UploadResult.Failure -> { /* stays in pending/ — retried next trigger */ }
        }
    }
}
