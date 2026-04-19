package com.travelmonk.core.logger.upload

import android.util.Log
import com.travelmonk.core.logger.LogEvent
import java.io.File

/**
 * Default RemoteLogSender used when no real backend is configured.
 * Logs upload attempts to Logcat so behaviour is visible during development.
 *
 * Replace with [FirebaseLogSender] or [DatadogLogSender] in production:
 *   TravelMonkLogger.init(context, isDebugBuild = BuildConfig.DEBUG, remote = FirebaseLogSender())
 */
internal class DummyHttpSender : RemoteLogSender {

    override suspend fun sendBatch(file: File): UploadResult {
        Log.d(TAG, "DummyHttpSender: would upload ${file.name} (${file.length()} bytes)")
        return UploadResult.Success
    }

    override suspend fun sendCritical(event: LogEvent): UploadResult {
        Log.d(TAG, "DummyHttpSender: would send critical event [${event.level}] ${event.tag}: ${event.message}")
        return UploadResult.Success
    }

    private companion object {
        private const val TAG = "TravelMonkLogger"
    }
}
