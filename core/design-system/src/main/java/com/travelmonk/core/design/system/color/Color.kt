package com.travelmonk.core.design.system.color

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand Palette — raw color atoms. These are the single source of truth for all
// brand colors used across the app. Never use hex values directly in UI code;
// always reference these constants so rebrand changes are one-place edits.
val TravelOrange = Color(0xFFFF5722)
val TravelBlue = Color(0xFF2196F3)
val TravelGreen = Color(0xFF4CAF50)
val TravelYellow = Color(0xFFFFC107)

// Semantic / Functional Palette — colors that carry intent (error, success, warning).
// Kept separate from brand colors so they can evolve independently.
val ErrorRed = Color(0xFFB00020)
val SuccessGreen = Color(0xFF4CAF50)
val WarningAmber = Color(0xFFFFA000)

// Neutral Palette — surface and background pairs for light/dark themes.
// Each surface has a matching "On" color that guarantees sufficient contrast.
val BackgroundLight = Color(0xFFF8F9FB)
val BackgroundDark = Color(0xFF121212)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)
val OnBackgroundLight = Color(0xFF000000)
val OnBackgroundDark = Color(0xFFFFFFFF)
val GrayText = Color(0xFF8E8E93)
val SurfaceVariantLight = Color(0xFFF1F4F8)
val SurfaceVariantDark = Color(0xFF2D2F36)

/**
 * Semantic color contract for the TravelMonk design system.
 *
 * Marked @Immutable so Compose can skip recompositions when the reference
 * hasn't changed. All properties are vals — mutating a color means creating
 * a new instance, which Compose detects and recomposes only what's needed.
 */
@Immutable
data class TravelMonkColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val grayText: Color,
    val surfaceVariant: Color,
    val isLight: Boolean
)

/**
 * CompositionLocal key that carries [TravelMonkColors] down the composition tree
 * without explicit parameter passing.
 *
 * Why staticCompositionLocalOf (not compositionLocalOf):
 * - compositionLocalOf  → triggers recomposition only in composables that READ the value.
 * - staticCompositionLocalOf → triggers full recomposition of the entire subtree when
 *   the value changes, but is faster to READ because no snapshot tracking is needed.
 *
 * Colors change at most once (light ↔ dark theme switch) and are read by nearly
 * every composable in the tree, so the cheaper read cost of staticCompositionLocalOf
 * outweighs the occasional full-subtree recompose on theme change.
 *
 * Default value: light-mode colors used as a fallback when this Local is read outside
 * a [TravelMonkTheme] wrapper (e.g. standalone Compose Previews). In production the
 * value is always overridden by the [CompositionLocalProvider] inside TravelMonkTheme.
 */
val LocalTravelMonkColors = staticCompositionLocalOf {
    TravelMonkColors(
        primary = TravelOrange,
        secondary = TravelBlue,
        tertiary = TravelGreen,
        background = BackgroundLight,
        surface = SurfaceLight,
        error = ErrorRed,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = OnBackgroundLight,
        onSurface = OnBackgroundLight,
        grayText = GrayText,
        surfaceVariant = SurfaceVariantLight,
        isLight = true
    )
}
