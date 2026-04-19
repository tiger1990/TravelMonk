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
    private val sender: RemoteLogSender
) {
    fun start(scope: CoroutineScope) {
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
        val claimedFile = fileManager.claimFileForUpload(file) ?: return

        // try-finally ensures releaseFile() is always called if sendBatch() throws
        // (e.g. IOException, OutOfMemoryError). Without this, a crash inside sendBatch()
        // leaves the file stuck in uploading/ until the next app start triggers
        // cleanupOrphanedUploads(). The exception is re-thrown so the coroutine
        // scope can surface it rather than silently swallowing it.
        try {
            when (sender.sendBatch(claimedFile)) {
                is UploadResult.Success -> fileManager.deleteAfterUpload(claimedFile)
                is UploadResult.Failure -> fileManager.releaseFile(claimedFile)
            }
        } catch (t: Throwable) {
            fileManager.releaseFile(claimedFile)
            throw t
        }
    }
}
