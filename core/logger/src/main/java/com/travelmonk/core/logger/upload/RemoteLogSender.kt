package com.travelmonk.core.logger.upload

import com.travelmonk.core.logger.LogEvent
import java.io.File

/**
 * Contract for remote log delivery.
 * Implement this interface to plug in Firebase, Datadog, or any custom backend.
 *
 * @see DummyHttpSender  active default implementation
 * @see FirebaseLogSender documented stub for Firebase integration
 * @see DatadogLogSender  documented stub for Datadog integration
 */
interface RemoteLogSender {
    /**
     * Batch-upload a rotated log file (JSON-lines format, one LogEvent per line).
     * Called by [LogUploadOrchestrator] on each file rotation and every 12 hours.
     * Return [UploadResult.Success] to trigger file deletion from pending/.
     */
    suspend fun sendBatch(file: File): UploadResult

    /**
     * Immediately upload a single critical (ERROR-level) event.
     * Best-effort — failure is silently ignored; the file pipeline is the safety net.
     */
    suspend fun sendCritical(event: LogEvent): UploadResult
}

sealed class UploadResult {
    object Success : UploadResult()
    data class Failure(val retryable: Boolean, val cause: Throwable) : UploadResult()
}
