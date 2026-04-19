package com.travelmonk.core.logger

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.FileOutputStream

/**
 * Manages log file I/O with a two-directory rotation pattern.
 *
 * Directory layout:
 *   files/logs/writing/current.log   — active write target (always this name)
 *   files/logs/pending/log_<ts>.txt  — rotated files awaiting upload
 *
 * On rotation: current.log is moved atomically to pending/log_<timestamp>.txt,
 * then a fresh current.log is created. Uploader only reads pending/ — never writing/.
 * On app restart: all files in pending/ are safe to upload (no partial writes).
 */
class LogFileManager(context: Context) {

    companion object {
        private const val MAX_FILE_SIZE = 512 * 1024L // 512 KB
    }

    private val writingDir = File(context.filesDir, "logs/writing").apply { mkdirs() }
    private val pendingDir = File(context.filesDir, "logs/pending").apply { mkdirs() }
    private val activeFile = File(writingDir, "current.log")

    private val _rotationEvents = MutableSharedFlow<File>(replay = 0, extraBufferCapacity = 10)
    val rotationEvents: SharedFlow<File> = _rotationEvents.asSharedFlow()

    init {
        if (!activeFile.exists()) activeFile.createNewFile()
    }

    @Synchronized
    fun writeBatch(logs: List<String>) {
        if (activeFile.length() >= MAX_FILE_SIZE) rotate()
        FileOutputStream(activeFile, true).bufferedWriter().use { writer ->
            logs.forEach { writer.append(it) }
            writer.flush()
        }
        // Rotate after writing in case a single large batch pushed the file over the limit
        if (activeFile.length() >= MAX_FILE_SIZE) rotate()
    }

    private fun rotate() {
        val dest = File(pendingDir, "log_${System.currentTimeMillis()}.txt")
        activeFile.renameTo(dest)
        activeFile.createNewFile()
        _rotationEvents.tryEmit(dest)
    }

    fun getPendingFiles(): List<File> =
        pendingDir.listFiles()?.sortedBy { it.lastModified() } ?: emptyList()

    fun getActiveFile(): File = activeFile

    fun deleteAfterUpload(file: File) { file.delete() }
}
