package com.travelmonk.core.logger

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

/**
 * Single-consumer actor coroutine that reads LogEvents from the channel,
 * batches them (up to 50 or after 5s idle), formats as JSON, and writes to disk.
 *
 * Single writer guarantees no concurrent access to LogFileManager.writeBatch().
 */
internal class LogProcessor(
    private val channel: ReceiveChannel<LogEvent>,
    private val fileManager: LogFileManager,
    private val scope: CoroutineScope
) {

    fun start() {
        scope.launch {
            val buffer = mutableListOf<LogEvent>()
            while (isActive) {
                val event = withTimeoutOrNull(5_000L) { channel.receive() }
                if (event != null) buffer.add(event)
                if (buffer.size >= 50 || (buffer.isNotEmpty() && event == null)) {
                    fileManager.writeBatch(buffer.map { format(it) })
                    buffer.clear()
                }
            }
            // Drain remaining on shutdown
            if (buffer.isNotEmpty()) fileManager.writeBatch(buffer.map { format(it) })
        }
    }

    private fun format(e: LogEvent): String = buildString {
        append("""{"ts":${e.timestamp},"level":"${e.level.name}","tag":${JSONObject.quote(e.tag)}""")
        append(""","msg":${JSONObject.quote(e.message)}""")
        e.traceId?.let     { append(""","traceId":"$it"""") }
        e.spanId?.let      { append(""","spanId":"$it"""") }
        e.flow?.let        { append(""","flow":"$it"""") }
        e.launchStack?.let { append(""","launchStack":${JSONObject.quote(it)}""") }
        append(""","thread":${JSONObject.quote(e.thread)}""")
        if (e.metadata.isNotEmpty()) append(""","meta":${JSONObject(e.metadata)}""")
        e.throwable?.let { append(""","error":${JSONObject.quote(Log.getStackTraceString(it))}""") }
        append("}\n")
    }
}
