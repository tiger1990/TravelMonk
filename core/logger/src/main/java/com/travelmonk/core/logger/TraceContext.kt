package com.travelmonk.core.logger

import kotlinx.coroutines.ThreadContextElement
import java.util.UUID
import kotlin.coroutines.CoroutineContext

/**
 * Propagates traceId, spanId, flow name, and optional launchStack across coroutine boundaries
 * via ThreadContextElement — readable from any thread without requiring a coroutine context.
 *
 * Usage:
 *   withContext(TraceContext.new(flow = "LoginFlow")) {
 *       TravelMonkLogger.i("Tag", "start")             // flow = "LoginFlow"
 *       launch { TravelMonkLogger.d("Tag", "child") }  // same traceId + flow inherited
 *   }
 *
 * launchStack is captured automatically when TravelMonkLogger.init(isDebugBuild = true).
 * It records the call site where the coroutine was created — useful for debugging
 * "who triggered this flow?" without a debugger. Never captured in release builds.
 */
class TraceContext(
    val traceId: String,
    val spanId: String,
    val flow: String? = null,
    val launchStack: String? = null
) : ThreadContextElement<TraceContext?> {

    companion object Key : CoroutineContext.Key<TraceContext> {
        private val threadLocal = ThreadLocal<TraceContext?>()

        /**
         * Set once by TravelMonkLogger.init(isDebugBuild = true).
         * Controls whether launchStack is captured in new().
         * Internal so tests can toggle it; feature code never touches this.
         */
        internal var debugMode: Boolean = false

        fun current(): TraceContext? = threadLocal.get()

        /**
         * Create a new TraceContext with fresh UUIDs.
         *
         * @param flow Optional semantic name for this coroutine tree, e.g. "LoginFlow".
         *             Propagates to every child coroutine and every LogEvent produced within.
         */
        fun new(flow: String? = null): TraceContext = TraceContext(
            traceId     = UUID.randomUUID().toString(),
            spanId      = UUID.randomUUID().toString(),
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
