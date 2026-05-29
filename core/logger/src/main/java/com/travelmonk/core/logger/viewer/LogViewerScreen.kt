package com.travelmonk.core.logger.viewer

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.logger.LogLevel
import com.travelmonk.core.logger.viewer.ui.LogViewerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogViewerScreen(
    viewModel: LogViewerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LogViewerContent(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerContent(
    state: LogViewerState,
    onIntent: (LogViewerIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Log Viewer") }) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(LogViewerIntent.Search(it)) },
                placeholder = { Text("Search tag or message…") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedLevel == null,
                        onClick = { onIntent(LogViewerIntent.FilterByLevel(null)) },
                        label = { Text("All") }
                    )
                }
                items(LogLevel.entries.toTypedArray()) { level ->
                    FilterChip(
                        selected = state.selectedLevel == level,
                        onClick = { onIntent(LogViewerIntent.FilterByLevel(level)) },
                        label = { Text(level.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = levelColor(level).copy(alpha = 0.2f)
                        )
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val listState = rememberLazyListState()

                // G8: trigger LoadMore when the last visible item reaches the end of the list
                LaunchedEffect(listState) {
                    snapshotFlow {
                        val layoutInfo = listState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        totalItems > 0 && lastVisible >= totalItems - 1
                    }
                        .distinctUntilChanged()
                        .filter { it }
                        .collect { onIntent(LogViewerIntent.LoadMore) }
                }

                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(state.entries, key = { it.id }) { entry ->
                        LogEntryRow(entry = entry)
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// Single formatter shared across all LogEntryRow calls — composition runs on the
// main thread so there is no thread-safety concern, and object allocation per row
// per recomposition is eliminated.
private val logTimestampFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.ROOT)

@Composable
private fun LogEntryRow(entry: LogEntry) {
    val color = levelColor(entry.level)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = entry.level.name.first().toString(),
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = logTimestampFormatter.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = entry.tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
                if (entry.flow != null) {
                    Text(
                        text = entry.flow,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun levelColor(level: LogLevel): Color = when (level) {
    LogLevel.VERBOSE -> MaterialTheme.colorScheme.outline
    LogLevel.DEBUG   -> MaterialTheme.colorScheme.primary
    LogLevel.INFO    -> MaterialTheme.colorScheme.tertiary
    LogLevel.WARN    -> Color(0xFFE65100)
    LogLevel.ERROR   -> MaterialTheme.colorScheme.error
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun LogViewerContentPreview() {
    LogViewerContent(
        state = LogViewerState(
            entries = listOf(
                LogEntry("1", System.currentTimeMillis(), LogLevel.INFO, "HomeVM", "Screen loaded", flow = "HomeFlow"),
                LogEntry("2", System.currentTimeMillis() - 1000, LogLevel.ERROR, "AuthRepo", "Token refresh failed", flow = "LoginFlow"),
                LogEntry("3", System.currentTimeMillis() - 2000, LogLevel.DEBUG, "FlightRepo", "Fetching flights", flow = "SearchFlow"),
                LogEntry("4", System.currentTimeMillis() - 3000, LogLevel.WARN, "NetworkMgr", "Slow response >2s"),
                LogEntry("5", System.currentTimeMillis() - 4000, LogLevel.VERBOSE, "TraceCtx", "Span started")
            )
        ),
        onIntent = {}
    )
}
