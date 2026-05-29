package com.travelmonk.core.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Carries the outer Scaffold's bottom bar height down to scrollable content.
 *
 * TravelMonkApp intentionally ignores bottom innerPadding on NavDisplay so list
 * content flows behind the glass bottom bar. Screens with LazyColumns must add
 * this value as bottom contentPadding so the last item can scroll fully above the bar.
 *
 * Provided by TravelMonkApp. Defaults to 0.dp so previews and standalone tests work unaffected.
 */
val LocalNavContentPadding = compositionLocalOf { 0.dp as Dp }
