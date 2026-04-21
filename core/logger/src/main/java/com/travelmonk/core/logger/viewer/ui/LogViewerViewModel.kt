package com.travelmonk.core.logger.viewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmonk.core.logger.LogEvent
import com.travelmonk.core.logger.LogFileManager
import com.travelmonk.core.logger.viewer.LogEntry
import com.travelmonk.core.logger.viewer.LogViewerIntent
import com.travelmonk.core.logger.viewer.LogViewerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class LogViewerViewModel @Inject constructor(
    private val fileManager: LogFileManager
) : ViewModel() {

    // Must match the writer config in LogFileManager (explicitNulls = false).
    // ignoreUnknownKeys = true guards against schema evolution — old log files with
    // missing or extra fields deserialize safely instead of throwing.
    private val logJson = Json { explicitNulls = false; ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(LogViewerState())
    val state: StateFlow<LogViewerState> = _state.asStateFlow()

    private var allEntries: List<LogEntry> = emptyList()
    private var lastLoadedFileIndex = -1
    private var hasMoreLogs = true

    init {
        loadLogs(isInitial = true)
    }

    fun onIntent(intent: LogViewerIntent) {
        when (intent) {
            is LogViewerIntent.Search -> {
                _state.update { it.copy(query = intent.query) }
                applyFilter()
            }
            is LogViewerIntent.FilterByLevel -> {
                _state.update { it.copy(selectedLevel = intent.level) }
                applyFilter()
            }
            LogViewerIntent.Refresh -> loadLogs(isInitial = true)
            LogViewerIntent.LoadMore -> if (!state.value.isLoading && !state.value.isNextPageLoading && hasMoreLogs) {
                loadLogs(isInitial = false)
            }
            LogViewerIntent.ClearFilter -> {
                _state.update { it.copy(selectedLevel = null, query = "") }
                applyFilter()
            }
        }
    }

    private fun loadLogs(isInitial: Boolean) {
        viewModelScope.launch {
            if (isInitial) {
                _state.update { it.copy(isLoading = true, error = null) }
                lastLoadedFileIndex = -1
                hasMoreLogs = true
                allEntries = emptyList()
            } else {
                _state.update { it.copy(isNextPageLoading = true) }
            }

            val nextBatch = fetchNextBatch()
            if (nextBatch.isEmpty()) hasMoreLogs = false
            
            allEntries = allEntries + nextBatch
            
            _state.update { it.copy(
                isLoading = false, 
                isNextPageLoading = false,
                hasMore = hasMoreLogs 
            ) }
            applyFilter()
        }
    }

    private fun applyFilter() {
        val current = _state.value
        val filtered = allEntries.filter { entry ->
            (current.selectedLevel == null || entry.level == current.selectedLevel) &&
            (current.query.isBlank() ||
                entry.message.contains(current.query, ignoreCase = true) ||
                entry.tag.contains(current.query, ignoreCase = true) ||
                entry.flow?.contains(current.query, ignoreCase = true) == true)
        }
        _state.update { it.copy(entries = filtered) }
    }

    private suspend fun fetchNextBatch(): List<LogEntry> = withContext(Dispatchers.IO) {
        val batch = mutableListOf<LogEntry>()
        val pageSize = 500

        // 1. Initial Load: Start with active file
        if (lastLoadedFileIndex == -1) {
            fileManager.readActiveFileLines().asReversed().forEach { line ->
                parseLine(line)?.let { batch.add(it) }
            }
            lastLoadedFileIndex = 0
            if (batch.size >= pageSize) return@withContext batch
        }

        // 2. Load from pending files
        val pendingFiles = fileManager.getPendingFiles().reversed()
        val startIndex = if (lastLoadedFileIndex <= 0) 0 else lastLoadedFileIndex
        
        for (i in startIndex until pendingFiles.size) {
            val file = pendingFiles[i]
            file.bufferedReader().useLines { lines ->
                lines.toList().asReversed().forEach { line ->
                    parseLine(line)?.let { batch.add(it) }
                }
            }
            lastLoadedFileIndex = i + 1
            if (batch.size >= pageSize) break
        }

        batch
    }

    // Deserializes through the same LogEvent type used by the writer, giving compiler
    // safety on field renames. Any schema mismatch becomes a compile error, not a
    // silent null at runtime (the previous org.json approach had no such safety net).
    private fun parseLine(line: String): LogEntry? = runCatching {
        val event = logJson.decodeFromString<LogEvent>(line.trim())
        LogEntry(
            id = "${event.timestamp}_${event.tag}_${event.message.hashCode()}",
            timestamp = event.timestamp,
            level = event.level,
            tag = event.tag,
            message = event.message,
            traceId = event.traceId,
            spanId = event.spanId,
            flow = event.flow,
            launchStack = event.launchStack
        )
    }.getOrNull()
}
