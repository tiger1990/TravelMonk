package com.travelmonk.core.logger

import android.content.Context
import android.util.Log
import com.travelmonk.core.logger.upload.CriticalEventUploader
import com.travelmonk.core.logger.upload.DummyHttpSender
import com.travelmonk.core.logger.upload.LogUploadOrchestrator
import com.travelmonk.core.logger.upload.LogUploadWorker
import com.travelmonk.core.logger.upload.RemoteLogSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Public logging API for TravelMonk.
 *
 * Initialise once in Application.onCreate():
 *   TravelMonkLogger.init(context, isDebugBuild = BuildConfig.DEBUG)
 */
object TravelMonkLogger {

    private val initialized = AtomicBoolean(false)
    private val initLock = ReentrantLock()

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
    // @Volatile ensures the reference swap in updateGlobalMetadata() is visible across
    // threads without a lock. Maps are immutable so the swap itself is atomic.
    @Volatile private var globalMetadata: Map<String, String> = emptyMap()

    fun init(
        context: Context,
        fileManager: LogFileManager,
        isDebugBuild: Boolean = false,
        remote: RemoteLogSender? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        initialMetadata: Map<String, String> = emptyMap()
    ) {
        if (initialized.get()) return
        initLock.withLock {
            if (initialized.get()) return
            this.scope = CoroutineScope(SupervisorJob() + dispatcher)
            this.isDebug = isDebugBuild
            this.globalMetadata = initialMetadata
            TraceContext.debugMode = isDebugBuild

            val appContext = context.applicationContext
            this.fileManager = fileManager
            this.remoteSender = remote ?: DummyHttpSender()

            LogProcessor(logChannel, fileManager).start(this.scope)
            CriticalEventUploader(criticalChannel, remoteSender!!).start(this.scope)
            LogUploadOrchestrator(fileManager, remoteSender!!).start(this.scope)
            
            // Scheduling WorkManager can involve disk I/O (DB init), 
            // so we move it to the background scope.
            this.scope.launch {
                LogUploadWorker.schedule(appContext)
            }

            initialized.set(true)
        }
    }

    internal fun resetForTest() {
        scope.cancel()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        initialized.set(false)
        fileManager = null
        remoteSender = null
        isDebug = false
        globalMetadata = emptyMap()
        TraceContext.debugMode = false
        
        // Purge stale logs from channels to ensure test isolation
        while (logChannel.tryReceive().isSuccess) { /* drain stale events for test isolation */ }
        while (criticalChannel.tryReceive().isSuccess) { /* drain stale events for test isolation */ }
    }

    /**
     * Merges [entries] into the global metadata applied to every subsequent log event.
     * Local metadata passed to individual log calls takes precedence on key collision.
     * Safe to call from any thread at any time after [init] — e.g. after login to attach userId.
     */
    fun updateGlobalMetadata(entries: Map<String, String>) {
        globalMetadata = globalMetadata + entries
    }

    fun v(tag: String, msg: String, metadata: Map<String, String> = emptyMap()) =
        log(LogLevel.VERBOSE, tag, msg, metadata = metadata)

    fun d(tag: String, msg: String, metadata: Map<String, String> = emptyMap()) =
        log(LogLevel.DEBUG, tag, msg, metadata = metadata)

    fun i(tag: String, msg: String, metadata: Map<String, String> = emptyMap()) =
        log(LogLevel.INFO, tag, msg, metadata = metadata)

    fun w(tag: String, msg: String, throwable: Throwable? = null) =
        log(LogLevel.WARN, tag, msg, throwable = throwable)

    fun e(tag: String, msg: String, throwable: Throwable? = null, metadata: Map<String, String> = emptyMap()) =
        log(LogLevel.ERROR, tag, msg, throwable = throwable, metadata = metadata)

    private fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap()
    ) {
        if (!initialized.get()) return
        val trace = TraceContext.current()
        
        // Performance optimization: Avoid map merging if local metadata is empty
        val finalMetadata = if (metadata.isEmpty()) globalMetadata else globalMetadata + metadata
        
        val event = LogEvent(
            timestamp   = System.currentTimeMillis(),
            level       = level,
            tag         = tag,
            message     = message,
            stacktrace  = LogEvent.formatThrowable(throwable),
            traceId     = trace?.traceId,
            spanId      = trace?.spanId,
            flow        = trace?.flow,
            launchStack = trace?.launchStack,
            metadata    = finalMetadata
        )
        logChannel.trySend(event)
        if (isDebug) logcat(event)
        if (level == LogLevel.ERROR) criticalChannel.trySend(event)
    }

    private fun logcat(event: LogEvent) {
        val fullMsg = if (event.stacktrace != null) "${event.message}\n${event.stacktrace}" else event.message
        when (event.level) {
            LogLevel.VERBOSE -> Log.v(event.tag, fullMsg)
            LogLevel.DEBUG   -> Log.d(event.tag, fullMsg)
            LogLevel.INFO    -> Log.i(event.tag, fullMsg)
            LogLevel.WARN    -> Log.w(event.tag, fullMsg)
            LogLevel.ERROR   -> Log.e(event.tag, fullMsg)
        }
    }
}
