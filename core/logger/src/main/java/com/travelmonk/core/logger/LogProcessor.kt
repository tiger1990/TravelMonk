package com.travelmonk.core.logger

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Single-consumer actor coroutine that reads LogEvents from the channel,
 * batches them (up to 50 or after 5s idle) and writes to disk.
 *
 * Single writer guarantees no concurrent access to LogFileManager.writeBatch().
 */
internal class LogProcessor(
    private val channel: ReceiveChannel<LogEvent>,
    private val fileManager: LogFileManager,
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            val buffer = mutableListOf<LogEvent>()
            try {
                while (isActive) {
                    val event = withTimeoutOrNull(5_000L) { channel.receive() }
                    if (event != null) buffer.add(event)
                    
                    if (buffer.size >= 50 || (buffer.isNotEmpty() && event == null)) {
                        fileManager.writeBatch(buffer)
                        buffer.clear()
                    }
                }
            } finally {
                // Ensure remaining logs are written even during cancellation
                if (buffer.isNotEmpty()) {
                    withContext(NonCancellable) {
                        fileManager.writeBatch(buffer)
                    }
                }
            }
        }
    }
}
