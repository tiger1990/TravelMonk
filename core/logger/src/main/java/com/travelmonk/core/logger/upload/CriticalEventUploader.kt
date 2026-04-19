package com.travelmonk.core.logger.upload

import com.travelmonk.core.logger.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

/**
 * Consumes ERROR-level events from [criticalChannel] and attempts immediate remote delivery.
 *
 * Best-effort — network failures are silently swallowed.
 * The file pipeline guarantees no data loss regardless of this uploader's outcome.
 */
internal class CriticalEventUploader(
    private val criticalChannel: ReceiveChannel<LogEvent>,
    private val sender: RemoteLogSender,
    private val scope: CoroutineScope
) {
    fun start() {
        scope.launch {
            for (event in criticalChannel) {
                try {
                    sender.sendCritical(event)
                } catch (_: Exception) {
                    // Intentionally swallowed — file write is the safety net
                }
            }
        }
    }
}
