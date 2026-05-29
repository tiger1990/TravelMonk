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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger
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

    // G1: track the active load coroutine so Refresh can cancel an in-flight LoadMore
    private var loadJob: Job? = null
    // G1: generation counter guards against a cancelled job's finally clearing the new job's loading state
    private var loadGeneration = 0
    // G2: track the active filter coroutine so a new filter cancels the previous one
    private var filterJob: Job? = null
    // G9: monotonic counter for unique LazyColumn keys — avoids hashCode collisions
    private val entryCounter = AtomicInteger(0)

    init {
        loadLogs(isInitial = true)
    }

    fun onIntent(intent: LogViewerIntent) {
        when (intent) {
            is LogViewerIntent.Search -> {
                _state.update { it.copy(query = intent.query) }
                launchFilter()
            }
            is LogViewerIntent.FilterByLevel -> {
                _state.update { it.copy(selectedLevel = intent.level) }
                launchFilter()
            }
            LogViewerIntent.Refresh -> loadLogs(isInitial = true)
            LogViewerIntent.LoadMore -> if (!state.value.isLoading && !state.value.isNextPageLoading && hasMoreLogs) {
                loadLogs(isInitial = false)
            }
            LogViewerIntent.ClearFilter -> {
                _state.update { it.copy(selectedLevel = null, query = "") }
                launchFilter()
            }
        }
    }

    private fun loadLogs(isInitial: Boolean) {
        loadJob?.cancel()
        val myGeneration = ++loadGeneration
        loadJob = viewModelScope.launch {
            if (isInitial) {
                _state.update { it.copy(isLoading = true, isNextPageLoading = false, error = null) }
                lastLoadedFileIndex = -1
                hasMoreLogs = true
                allEntries = emptyList()
            } else {
                _state.update { it.copy(isNextPageLoading = true) }
            }
            try {
                val nextBatch = fetchNextBatch()
                if (nextBatch.isEmpty()) hasMoreLogs = false
                allEntries = allEntries + nextBatch
                _state.update { it.copy(isLoading = false, isNextPageLoading = false, hasMore = hasMoreLogs) }
                applyFilter()
            } finally {
                // Only clear loading flags if we are still the active job — a newer Refresh
                // has already set isLoading = true and must not have it cleared by our finally.
                if (myGeneration == loadGeneration) {
                    _state.update { it.copy(isLoading = false, isNextPageLoading = false) }
                }
            }
        }
    }

    // G2: cancel previous filter before starting a new one — prevents stale results from a
    // slow Dispatchers.Default run completing after a newer filter and overwriting fresher state.
    private fun launchFilter() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch { applyFilter() }
    }

    // suspend so filtering runs on Dispatchers.Default — avoids blocking the main thread
    // when allEntries grows to hundreds of entries.
    private suspend fun applyFilter() {
        val current = _state.value
        // Capture the list reference on the calling dispatcher (Main) before switching —
        // reading the var inside withContext(Default) would be a data race.
        val snapshot = allEntries
        val filtered = withContext(Dispatchers.Default) {
            snapshot.filter { entry ->
                (current.selectedLevel == null || entry.level == current.selectedLevel) &&
                (current.query.isBlank() ||
                    entry.message.contains(current.query, ignoreCase = true) ||
                    entry.tag.contains(current.query, ignoreCase = true) ||
                    entry.flow?.contains(current.query, ignoreCase = true) == true)
            }
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
            id = entryCounter.getAndIncrement().toString(),
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
