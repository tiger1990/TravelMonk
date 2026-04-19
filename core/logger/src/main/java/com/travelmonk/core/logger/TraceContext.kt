package com.travelmonk.core.logger

import kotlinx.coroutines.ThreadContextElement
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

/**
 * Propagates traceId, spanId, flow name, and optional launchStack across coroutine boundaries
 * via ThreadContextElement — readable from any thread without requiring a coroutine context.
 */
class TraceContext(
    val traceId: String,
    val spanId: String,
    val flow: String? = null,
    val launchStack: String? = null
) : ThreadContextElement<TraceContext?> {

    companion object Key : CoroutineContext.Key<TraceContext> {
        private val threadLocal = ThreadLocal<TraceContext?>()
        private val counter = AtomicLong(System.currentTimeMillis())
        private val sessionPrefix = Random.nextInt(1000, 9999).toString()

        /**
         * Generates a fast, unique-enough ID for tracing.
         * Format: <session>-<counter> (e.g., TM4821-1715234567001)
         * Avoids blocking SecureRandom/UUID for high-throughput logging.
         */
        private fun generateFastId(): String = "TM$sessionPrefix-${counter.incrementAndGet()}"

        internal var debugMode: Boolean = false

        fun current(): TraceContext? = threadLocal.get()

        fun new(flow: String? = null): TraceContext = TraceContext(
            traceId     = generateFastId(),
            spanId      = generateFastId(),
            flow        = flow,
            launchStack = if (debugMode) captureStack() else null
        )

        /**
         * Captures the top 8 frames of the current call stack, skipping internal TraceContext
         * frames. Formatted as "ClassName.method:line ← ..." for readability.
         */
        private fun captureStack(): String =
            Thread.currentThread().stackTrace
                .drop(3)   // skip: Thread.getStackTrace / captureStack / new()
                .take(8)   // enough context to find the caller
                .joinToString(" ← ") {
                    "${it.className.substringAfterLast('.')}.${it.methodName}:${it.lineNumber}"
                }
    }

    override val key: CoroutineContext.Key<TraceContext> get() = Key

    override fun updateThreadContext(context: CoroutineContext): TraceContext? {
        val old = threadLocal.get()
        threadLocal.set(this)
        return old
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: TraceContext?) {
        threadLocal.set(oldState)
    }
}
