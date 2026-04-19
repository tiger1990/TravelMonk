package com.travelmonk.core.logger.upload

import com.travelmonk.core.logger.LogEvent
import java.io.File

/**
 * Firebase Cloud Storage + Crashlytics integration stub.
 *
 * ## Setup
 * 1. Add dependencies in your app module:
 *    ```
 *    implementation("com.google.firebase:firebase-crashlytics-ktx")
 *    implementation("com.google.firebase:firebase-storage-ktx")
 *    ```
 * 2. Initialize Firebase in Application.onCreate() before TravelMonkLogger.init().
 * 3. Replace the TODO blocks below with real Firebase calls.
 *
 * ## Batch upload strategy
 * Firebase has no native structured log ingestion API.
 * Recommended pattern:
 *   Cloud Storage bucket → Cloud Function trigger → BigQuery for querying
 *
 * ## Critical event strategy
 * Use FirebaseCrashlytics.getInstance().log() for ERROR events.
 * These appear in Crashlytics "Logs" tab alongside crash reports.
 */
class FirebaseLogSender : RemoteLogSender {

    override suspend fun sendBatch(file: File): UploadResult {
        return try {
            // TODO: val storageRef = Firebase.storage.reference.child("logs/${file.name}")
            // TODO: storageRef.putFile(android.net.Uri.fromFile(file)).await()
            UploadResult.Success
        } catch (e: Exception) {
            UploadResult.Failure(retryable = true, cause = e)
        }
    }

    override suspend fun sendCritical(event: LogEvent): UploadResult {
        return try {
            // TODO: FirebaseCrashlytics.getInstance().log("[${event.level}] ${event.tag}: ${event.message}")
            // TODO: event.throwable?.let { FirebaseCrashlytics.getInstance().recordException(it) }
            UploadResult.Success
        } catch (e: Exception) {
            UploadResult.Failure(retryable = false, cause = e)
        }
    }
}
