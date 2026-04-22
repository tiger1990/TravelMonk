package com.travelmonk.core.design.system.sizing

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Component sizing tokens for the TravelMonk design system.
 *
 * Unlike Spacing (which controls gaps between elements) and Radius (which
 * controls corner shapes), Dimensions defines the fixed sizes of UI components
 * themselves — icons, bars, cards, and buttons.
 *
 * Keeping these as tokens (rather than hardcoding in each composable) means a
 * single change here propagates everywhere, and feature screens never need to
 * agree on "what height is the bottom bar again?".
 *
 * Marked @Immutable so Compose treats the object as stable and skips
 * recompositions when the reference hasn't changed.
 */
@Immutable
data class Dimensions(
    // Icon sizes — maps to Material icon size guidance (16/24/48dp)
    val iconExtraSmall: Dp = 14.dp,
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 24.dp,   // default touch-target icon size
    val iconLarge: Dp = 48.dp,
    val iconExtraLarge: Dp = 80.dp,

    // Layout / structural component heights
    val headerHeight: Dp = 100.dp,
    val flightHeaderHeight: Dp = 120.dp,  // taller header for flight search hero
    val buttonHeight: Dp = 56.dp,          // meets 48dp minimum touch target + padding
    val cardElevation: Dp = 2.dp,
    val cardElevationLarge: Dp = 8.dp,
    val bottomBarHeight: Dp = 80.dp,

    // Content component sizes
    val imageCardHeight: Dp = 160.dp,
    val heroImageHeight: Dp = 300.dp,
    val splashIconSize: Dp = 240.dp,
    val fabSize: Dp = 44.dp,
    val categoryIconSize: Dp = 60.dp,
    val bookingButtonWidth: Dp = 160.dp
)

/**
 * CompositionLocal key that carries [Dimensions] down the composition tree.
 *
 * staticCompositionLocalOf is faster and does not trigger recomposition,
 * making it ideal for rarely changing values like design tokens (e.g., dimensions).
 * compositionLocalOf triggers recomposition and should only be used for dynamic values,
 * so staticCompositionLocalOf is the correct choice for dimensions.
 *
 * Why staticCompositionLocalOf (not compositionLocalOf):
 * - Component dimensions are design constants — they don't vary by theme, locale,
 *   or user preference at runtime. staticCompositionLocalOf has zero snapshot
 *   tracking overhead on reads, which is the right trade-off for values that
 *   are written once and never updated during the session.
 *
 * Default value: the standard component sizing scale. Accessible via
 * TravelMonkTheme.dimensions in any composable inside TravelMonkTheme.
 */
val LocalDimensions = staticCompositionLocalOf { Dimensions() }
