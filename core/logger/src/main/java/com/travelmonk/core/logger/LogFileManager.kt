package com.travelmonk.core.logger

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

/**
 * Manages log file I/O with a three-directory rotation pattern.
 *
 * Directory layout:
 *   files/logs/writing/current.log    — active write target
 *   files/logs/pending/log_<ts>.txt   — rotated files awaiting upload
 *   files/logs/uploading/log_<ts>.txt — files currently being transmitted (Atomic Claim)
 */
class LogFileManager(context: Context) {

    companion object {
        private const val MAX_FILE_SIZE = 512 * 1024L // 512 KB
        private const val MAX_PENDING_FILES = 10      // ~5MB total storage limit for rotated logs
    }

    private val writingDir by lazy { File(context.filesDir, "logs/writing") }
    private val pendingDir by lazy { File(context.filesDir, "logs/pending") }
    private val uploadingDir by lazy { File(context.filesDir, "logs/uploading") }
    private val activeFile by lazy { File(writingDir, "current.log") }

    private val writeMutex = Mutex()
    // No @Volatile needed — ensureInitialized() is always called inside writeMutex.withLock {},
    // which already provides mutual exclusion and memory visibility.
    private var isInitialized = false

    /**
     * explicitNulls = false omits null fields (traceId, spanId, flow, stacktrace, launchStack)
     * from the JSON output when they are null. A plain INFO log is ~60% smaller as a result.
     * The log viewer uses optString/optLong which returns defaults for missing keys — safe.
     */
    private val json = Json { explicitNulls = false }

    private val _rotationEvents = MutableSharedFlow<File>(replay = 0, extraBufferCapacity = 10)
    val rotationEvents: SharedFlow<File> = _rotationEvents.asSharedFlow()

    init {
        // No disk I/O in constructor to avoid StrictMode violations on main thread.
        // Initialization is deferred to ensure it happens on a background thread.
    }

    private fun ensureInitialized() {
        if (isInitialized) return
        try {
            if (!writingDir.exists()) writingDir.mkdirs()
            if (!pendingDir.exists()) pendingDir.mkdirs()
            if (!uploadingDir.exists()) uploadingDir.mkdirs()
            if (!activeFile.exists()) activeFile.createNewFile()
            cleanupOrphanedUploads()
            isInitialized = true
        } catch (_: Exception) {
            // Fail-safe: If storage is completely blocked, we just won't log
        }
    }

    /**
     * Writes a batch of events directly to disk.
     * Wrapped in try-catch to ensure logging failures never crash the app.
     */
    suspend fun writeBatch(events: List<LogEvent>): Unit = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            ensureInitialized()
            try {
                if (activeFile.length() >= MAX_FILE_SIZE) rotate()

                FileOutputStream(activeFile, true).bufferedWriter().use { writer ->
                    events.forEach { event ->
                        writer.append(json.encodeToString(event))
                        writer.append("\n")
                    }
                    writer.flush()
                }

                if (activeFile.length() >= MAX_FILE_SIZE) rotate()
            } catch (_: Exception) {
                // Logging should never crash the host app.
                // We'll let it fail silently or print to logcat if in debug.
            }
        }
    }

    // Regular function — no suspend calls inside. Called from within writeMutex.withLock {}.
    private fun rotate() {
        val dest = File(pendingDir, "log_${System.currentTimeMillis()}.txt")
        if (activeFile.exists()) {
            if (activeFile.renameTo(dest)) {
                activeFile.createNewFile()
                enforceRetention()
                _rotationEvents.tryEmit(dest)
            }
        }
    }

    /**
     * Prevents storage bloat by deleting the oldest pending logs if the total file count
     * across pending/ AND uploading/ exceeds MAX_PENDING_FILES.
     * Both directories are counted because uploading/ files are still consuming disk space —
     * counting only pending/ allowed actual usage to exceed the ~10MB cap during active uploads.
     */
    private fun enforceRetention() {
        val pendingFiles = getPendingFilesInternal()
        val uploadingCount = uploadingDir.listFiles()?.size ?: 0
        val totalCount = pendingFiles.size + uploadingCount
        if (totalCount > MAX_PENDING_FILES) {
            pendingFiles.take(totalCount - MAX_PENDING_FILES).forEach { it.delete() }
        }
    }

    /**
     * Atomically claims a file for upload by moving it to the uploading directory.
     * Returns the new file location, or null if the file was already claimed/deleted.
     */
    suspend fun claimFileForUpload(file: File): File? = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            ensureInitialized()
            if (!file.exists() || file.parentFile?.name != "pending") return@withLock null
            val dest = File(uploadingDir, file.name)
            if (file.renameTo(dest)) dest else null
        }
    }

    /**
     * Releases a failed upload back to the pending directory.
     */
    suspend fun releaseFile(file: File): Unit = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            ensureInitialized()
            if (!file.exists() || file.parentFile?.name != "uploading") return@withLock
            val dest = File(pendingDir, file.name)
            file.renameTo(dest)
        }
    }

    /**
     * On startup, move any stuck files from uploading back to pending.
     */
    private fun cleanupOrphanedUploads() {
        uploadingDir.listFiles()?.forEach { it.renameTo(File(pendingDir, it.name)) }
    }

    /**
     * Reads all lines from the active log file safely.
     * Uses the writeMutex to ensure we don't read while a write or rotation is in progress.
     */
    suspend fun readActiveFileLines(): List<String> = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            ensureInitialized()
            if (!activeFile.exists()) return@withLock emptyList()
            activeFile.readLines()
        }
    }

    /**
     * Public access to pending files, properly synchronized.
     */
    suspend fun getPendingFiles(): List<File> = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            ensureInitialized()
            getPendingFilesInternal()
        }
    }

    /**
     * Internal access to pending files without re-acquiring the lock.
     * Must only be called from contexts already holding writeMutex.
     */
    private fun getPendingFilesInternal(): List<File> =
        pendingDir.listFiles()?.sortedBy { it.lastModified() } ?: emptyList()

    suspend fun deleteAfterUpload(file: File): Unit = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            ensureInitialized()
            file.delete()
        }
    }
}
