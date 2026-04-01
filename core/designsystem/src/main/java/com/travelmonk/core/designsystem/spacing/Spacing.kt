package com.travelmonk.core.designsystem.spacing

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale for the TravelMonk design system.
 *
 * A fixed 4dp base grid — every value is a multiple of 4 to keep layouts
 * consistent and pixel-perfect across screen densities. Use these tokens
 * for padding, margin, and gap values instead of hardcoding dp values.
 *
 *   extraSmall (4dp)  — tight internal padding, icon-to-label gaps
 *   small      (8dp)  — default inner padding for compact components
 *   medium     (16dp) — standard content padding, card internal spacing
 *   large      (24dp) — section gaps, screen edge padding
 *   extraLarge (32dp) — hero spacing, large section separators
 *
 * Marked @Immutable so Compose treats the object as stable and skips
 * recompositions when the reference hasn't changed.
 */
@Immutable
data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp
)

/**
 * CompositionLocal key that carries [Spacing] down the composition tree.
 *
 * Why staticCompositionLocalOf (not compositionLocalOf):
 * - Spacing is a design constant — it never changes at runtime.
 *   staticCompositionLocalOf skips snapshot tracking entirely on reads,
 *   which is the correct choice for values that are set once and never updated.
 *
 * Default value: the standard 4dp-grid scale. Accessible via
 * [TravelMonkTheme.spacing] in any composable inside [TravelMonkTheme].
 */
val LocalSpacing = staticCompositionLocalOf { Spacing() }
