package com.travelmonk.core.logger

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val writingDir = File(context.filesDir, "logs/writing").apply { mkdirs() }
    private val pendingDir = File(context.filesDir, "logs/pending").apply { mkdirs() }
    private val uploadingDir = File(context.filesDir, "logs/uploading").apply { mkdirs() }
    private val activeFile = File(writingDir, "current.log")
    
    private val writeMutex = Mutex()
    /**
     * explicitNulls = false omits null fields (traceId, spanId, flow, stacktrace, launchStack)
     * from the JSON output when they are null. A plain INFO log is ~60% smaller as a result.
     * The log viewer uses optString/optLong which returns defaults for missing keys — safe.
     */
    private val json = Json { explicitNulls = false }

    private val _rotationEvents = MutableSharedFlow<File>(replay = 0, extraBufferCapacity = 10)
    val rotationEvents: SharedFlow<File> = _rotationEvents.asSharedFlow()

    init {
        try {
            if (!activeFile.exists()) activeFile.createNewFile()
            cleanupOrphanedUploads()
        } catch (_: Exception) {
            // Fail-safe: If storage is completely blocked, we just won't log
        }
    }

    /**
     * Writes a batch of events directly to disk.
     * Wrapped in try-catch to ensure logging failures never crash the app.
     */
    suspend fun writeBatch(events: List<LogEvent>) = writeMutex.withLock {
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
        val pendingFiles = getPendingFiles()
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
    fun claimFileForUpload(file: File): File? {
        if (!file.exists() || file.parentFile?.name != "pending") return null
        val dest = File(uploadingDir, file.name)
        return if (file.renameTo(dest)) dest else null
    }

    /**
     * Releases a failed upload back to the pending directory.
     */
    fun releaseFile(file: File) {
        if (!file.exists() || file.parentFile?.name != "uploading") return
        val dest = File(pendingDir, file.name)
        file.renameTo(dest)
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
    suspend fun readActiveFileLines(): List<String> = writeMutex.withLock {
        if (!activeFile.exists()) return@withLock emptyList()
        activeFile.readLines()
    }

    fun getPendingFiles(): List<File> =
        pendingDir.listFiles()?.sortedBy { it.lastModified() } ?: emptyList()

    fun deleteAfterUpload(file: File) { file.delete() }
}
