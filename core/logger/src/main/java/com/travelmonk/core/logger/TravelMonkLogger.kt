package com.travelmonk.core.logger

import android.content.Context
import android.util.Log
import com.travelmonk.core.logger.upload.CriticalEventUploader
import com.travelmonk.core.logger.upload.DummyHttpSender
import com.travelmonk.core.logger.upload.LogUploadOrchestrator
import com.travelmonk.core.logger.upload.LogUploadWorker
import com.travelmonk.core.logger.upload.RemoteLogSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Public logging API for TravelMonk.
 *
 * Initialise once in Application.onCreate():
 *   TravelMonkLogger.init(context, isDebugBuild = BuildConfig.DEBUG)
 *
 * Then use from anywhere:
 *   TravelMonkLogger.d("LoginVM", "User tapped login")
 *   TravelMonkLogger.e("AuthRepo", "Token refresh failed", throwable)
 *
 * With distributed tracing:
 *   withContext(TraceContext.new()) {
 *       TravelMonkLogger.i("Api", "Calling flights endpoint")
 *       launch { TravelMonkLogger.d("Api", "Parsing response") }  // shares traceId
 *   }
 */
object TravelMonkLogger {

    private val initialized = AtomicBoolean(false)

    /**
     * Active coroutine scope. Defaults to a production IO scope.
     * Tests may inject a TestScope via [init] to control execution with
     * advanceUntilIdle() instead of relying on real wall-clock delays.
     */
    private var scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val logChannel = Channel<LogEvent>(
        capacity = 1000,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val criticalChannel = Channel<LogEvent>(
        capacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    internal var fileManager: LogFileManager? = null
    internal var remoteSender: RemoteLogSender? = null
    private var isDebug: Boolean = false

    /**
     * Must be called once in Application.onCreate() before any logging.
     * Subsequent calls are no-ops (idempotent).
     *
     * @param context Application context.
     * @param isDebugBuild Pass BuildConfig.DEBUG from the app module. Controls Logcat output.
     * @param remote Optional remote sender. Defaults to DummyHttpSender (logs to console).
     * @param scope Coroutine scope for log processing. Defaults to a production IO scope.
     *              Inject a TestScope in tests to control execution with advanceUntilIdle().
     */
    fun init(
        context: Context,
        isDebugBuild: Boolean = false,
        remote: RemoteLogSender? = null,
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    ) {
        if (!initialized.compareAndSet(false, true)) return
        this.scope = scope
        isDebug = isDebugBuild
        TraceContext.debugMode = isDebugBuild
        val appContext = context.applicationContext
        fileManager = LogFileManager(appContext)
        remoteSender = remote ?: DummyHttpSender()
        LogProcessor(logChannel, fileManager!!, this.scope).start()
        CriticalEventUploader(criticalChannel, remoteSender!!, this.scope).start()
        LogUploadOrchestrator(fileManager!!, remoteSender!!, this.scope).start()
        LogUploadWorker.schedule(appContext)
    }

    /**
     * Cancels all running coroutines, drains channels, and resets all state to pre-init.
     * Call from @Before in tests to guarantee a clean slate for each test — eliminates
     * the race condition where multiple CriticalEventUploader instances compete for
     * events on the shared criticalChannel across test runs.
     */
    internal fun resetForTest() {
        scope.cancel()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        initialized.set(false)
        fileManager = null
        remoteSender = null
        isDebug = false
        TraceContext.debugMode = false
        while (logChannel.tryReceive().isSuccess) { /* drain stale events */ }
        while (criticalChannel.tryReceive().isSuccess) { /* drain stale events */ }
    }

    fun v(tag: String, msg: String, metadata: Map<String, Any?> = emptyMap()) =
        log(LogLevel.VERBOSE, tag, msg, metadata = metadata)

    fun d(tag: String, msg: String, metadata: Map<String, Any?> = emptyMap()) =
        log(LogLevel.DEBUG, tag, msg, metadata = metadata)

    fun i(tag: String, msg: String, metadata: Map<String, Any?> = emptyMap()) =
        log(LogLevel.INFO, tag, msg, metadata = metadata)

    fun w(tag: String, msg: String, throwable: Throwable? = null) =
        log(LogLevel.WARN, tag, msg, throwable = throwable)

    fun e(tag: String, msg: String, throwable: Throwable? = null, metadata: Map<String, Any?> = emptyMap()) =
        log(LogLevel.ERROR, tag, msg, throwable = throwable, metadata = metadata)

    /** Force-flush remaining buffer. Call from Application.onTerminate() or ProcessLifecycleOwner. */
    fun flush() {
        logChannel.close()
    }

    private fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, Any?> = emptyMap()
    ) {
        if (!initialized.get()) return
        val trace = TraceContext.current()
        val event = LogEvent(
            timestamp   = System.currentTimeMillis(),
            level       = level,
            tag         = tag,
            message     = message,
            throwable   = throwable,
            traceId     = trace?.traceId,
            spanId      = trace?.spanId,
            flow        = trace?.flow,
            launchStack = trace?.launchStack,
            metadata    = metadata
        )
        logChannel.trySend(event)
        if (isDebug) logcat(event)
        if (level == LogLevel.ERROR) criticalChannel.trySend(event)
    }

    private fun logcat(event: LogEvent) {
        when (event.level) {
            LogLevel.VERBOSE -> Log.v(event.tag, event.message)
            LogLevel.DEBUG   -> Log.d(event.tag, event.message)
            LogLevel.INFO    -> Log.i(event.tag, event.message)
            LogLevel.WARN    -> Log.w(event.tag, event.message, event.throwable)
            LogLevel.ERROR   -> Log.e(event.tag, event.message, event.throwable)
        }
    }
}
