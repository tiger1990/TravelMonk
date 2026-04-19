package com.travelmonk.core.logger.viewer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmonk.core.logger.LogFileManager
import com.travelmonk.core.logger.LogLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class LogViewerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val fileManager = LogFileManager(context)
    private val _state = MutableStateFlow(LogViewerState())
    val state: StateFlow<LogViewerState> = _state.asStateFlow()

    private var allEntries: List<LogEntry> = emptyList()

    init {
        loadLogs()
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
            LogViewerIntent.Refresh -> loadLogs()
            LogViewerIntent.ClearFilter -> {
                _state.update { it.copy(selectedLevel = null, query = "") }
                applyFilter()
            }
        }
    }

    private fun loadLogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            allEntries = withContext(Dispatchers.IO) { readAllLogEntries() }
            _state.update { it.copy(isLoading = false) }
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

    private fun readAllLogEntries(): List<LogEntry> =
        (fileManager.getPendingFiles() + fileManager.getActiveFile())
            .filter { it.exists() && it.length() > 0 }
            .flatMap { file -> file.readLines().mapNotNull { parseLine(it) } }
            .sortedByDescending { it.timestamp }

    private fun parseLine(line: String): LogEntry? = runCatching {
        val json = JSONObject(line.trim())
        LogEntry(
            id = "${json.optLong("ts")}_${json.optString("tag")}_${json.optString("msg").hashCode()}",
            timestamp = json.optLong("ts"),
            level = LogLevel.valueOf(json.optString("level", "DEBUG")),
            tag = json.optString("tag"),
            message = json.optString("msg"),
            traceId = json.optString("traceId").takeIf { it.isNotEmpty() },
            spanId = json.optString("spanId").takeIf { it.isNotEmpty() },
            flow = json.optString("flow").takeIf { it.isNotEmpty() },
            launchStack = json.optString("launchStack").takeIf { it.isNotEmpty() }
        )
    }.getOrNull()
}
