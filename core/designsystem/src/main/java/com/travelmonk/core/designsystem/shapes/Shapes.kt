package com.travelmonk.core.designsystem.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Corner radius scale for the TravelMonk design system.
 *
 * Consistent corner radii give the UI a unified "feel". Using a scale rather
 * than ad-hoc values ensures that cards, buttons, chips, and dialogs all follow
 * the same geometric language.
 *
 *   extraSmall (4dp)  — input fields, small tags
 *   small      (8dp)  — chips, compact cards
 *   medium     (16dp) — standard cards, bottom sheets
 *   large      (24dp) — large modals, feature cards
 *   extraLarge (32dp) — pill shapes, FABs, hero containers
 *
 * Marked @Immutable so Compose treats the object as stable and avoids
 * unnecessary recompositions when the reference hasn't changed.
 */
@Immutable
data class Radius(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp
)

/**
 * CompositionLocal key that carries [Radius] down the composition tree.
 *
 * Why staticCompositionLocalOf (not compositionLocalOf):
 * - Shape tokens are design constants with no runtime variants (no dark/light,
 *   no user preference). staticCompositionLocalOf has no snapshot overhead on reads,
 *   making it strictly faster than compositionLocalOf for values that never change.
 *
 * Default value: the standard radius scale. Accessible via
 * [TravelMonkTheme.radius] in any composable inside [TravelMonkTheme].
 */
val LocalRadius = staticCompositionLocalOf { Radius() }
