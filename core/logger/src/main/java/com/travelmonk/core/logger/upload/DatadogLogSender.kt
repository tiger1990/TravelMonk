package com.travelmonk.core.logger.upload

import com.travelmonk.core.logger.LogEvent
import java.io.File

/**
 * Datadog Mobile SDK integration stub.
 *
 * ## Setup
 * 1. Add dependency in your app module:
 *    ```
 *    implementation("com.datadoghq:dd-sdk-android-logs:<version>")
 *    ```
 * 2. Initialize Datadog in Application.onCreate() before TravelMonkLogger.init():
 *    ```kotlin
 *    val credentials = Credentials(clientToken, envName, variant, applicationId)
 *    Datadog.initialize(context, credentials, configuration, trackingConsent)
 *    val logsConfig = LogsConfiguration.Builder().build()
 *    Logs.enable(logsConfig)
 *    val ddLogger = Logger.Builder().setNetworkInfoEnabled(true).build()
 *    ```
 * 3. Replace TODO blocks below with real Datadog calls.
 *
 * ## Batch upload strategy
 * Parse each JSON line from the log file into a LogEventDto,
 * then call ddLogger.log() with structured attributes.
 * Datadog natively supports attribute maps — pass metadata directly.
 *
 * ## Critical event strategy
 * Call ddLogger.e() immediately for ERROR-level events.
 */
class DatadogLogSender : RemoteLogSender {

    override suspend fun sendBatch(file: File): UploadResult {
        return try {
            file.forEachLine { line ->
                // TODO: val dto = parseJsonLine(line)
                // TODO: ddLogger.log(dto.level.toDatadogPriority(), dto.message, dto.throwable, dto.metadata)
            }
            UploadResult.Success
        } catch (e: Exception) {
            UploadResult.Failure(retryable = true, cause = e)
        }
    }

    override suspend fun sendCritical(event: LogEvent): UploadResult {
        return try {
            // TODO: ddLogger.e(event.message, event.throwable, event.metadata)
            UploadResult.Success
        } catch (e: Exception) {
            UploadResult.Failure(retryable = false, cause = e)
        }
    }
}
