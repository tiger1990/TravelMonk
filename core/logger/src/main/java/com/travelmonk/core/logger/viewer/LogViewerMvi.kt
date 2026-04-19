package com.travelmonk.core.logger.viewer

import com.travelmonk.core.logger.LogLevel

data class LogViewerState(
    val entries: List<LogEntry> = emptyList(),
    val selectedLevel: LogLevel? = null,
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class LogEntry(
    val id: String,
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val traceId: String? = null,
    val spanId: String? = null,
    val flow: String? = null,
    val launchStack: String? = null
)

sealed interface LogViewerIntent {
    data class Search(val query: String) : LogViewerIntent
    data class FilterByLevel(val level: LogLevel?) : LogViewerIntent
    data object Refresh : LogViewerIntent
    data object ClearFilter : LogViewerIntent
}
