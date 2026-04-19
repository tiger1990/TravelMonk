package com.travelmonk.core.logger

import android.util.Log
import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class LogEvent(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val stacktrace: String? = null,
    val traceId: String? = null,
    val spanId: String? = null,
    val flow: String? = null,
    val launchStack: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val thread: String = Thread.currentThread().name
) {
    companion object {
        private const val MAX_STACKTRACE_CHARS = 4000 // Safe limit for most JSON collectors

        /**
         * Converts a Throwable to a safe, truncated stacktrace string.
         * If the trace is too long, we preserve the beginning and end to keep the root cause.
         */
        fun formatThrowable(throwable: Throwable?): String? {
            if (throwable == null) return null
            val fullTrace = Log.getStackTraceString(throwable)
            
            return if (fullTrace.length > MAX_STACKTRACE_CHARS) {
                val head = fullTrace.take(MAX_STACKTRACE_CHARS / 2)
                val tail = fullTrace.takeLast(MAX_STACKTRACE_CHARS / 2)
                "$head\n\n[... TRUNCATED ...]\n\n$tail"
            } else {
                fullTrace
            }
        }
    }
}
