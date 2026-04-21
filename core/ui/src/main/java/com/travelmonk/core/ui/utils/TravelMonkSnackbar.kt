package com.travelmonk.core.ui.utils

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.travelmonk.core.design.system.theme.TravelMonkTheme

/**
 * Themed Snackbar host for one-shot transient messages (errors, confirmations).
 *
 * Usage:
 * ```
 * val snackbarHostState = remember { SnackbarHostState() }
 *
 * Scaffold(snackbarHost = { TravelMonkSnackbarHost(snackbarHostState) }) { ... }
 *
 * // In effect collector:
 * snackbarHostState.showSnackbar(message)
 * ```
 */
@Composable
fun TravelMonkSnackBarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = TravelMonkTheme.colors.onSurface,
            contentColor = TravelMonkTheme.colors.surface,
            actionColor = TravelMonkTheme.colors.primary
        )
    }
}
