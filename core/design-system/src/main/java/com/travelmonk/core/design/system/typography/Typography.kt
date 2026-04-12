package com.travelmonk.core.design.system.typography

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography scale for the TravelMonk design system.
 *
 * Each slot maps to a specific role in the visual hierarchy:
 *   headlineLarge  — hero titles, splash screens
 *   headlineMedium — section headers, screen titles
 *   titleLarge     — card titles, prominent labels
 *   bodyLarge      — primary readable content
 *   labelMedium    — tab labels, chips, captions
 *   caption        — fine print, metadata, timestamps
 *
 * Marked @Immutable so Compose treats the entire object as a stable input
 * and skips recompositions when the reference hasn't changed.
 */
@Immutable
data class TravelMonkTypography(
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val titleLarge: TextStyle,
    val bodyLarge: TextStyle,
    val labelMedium: TextStyle,
    val caption: TextStyle
)

/**
 * CompositionLocal key that carries [TravelMonkTypography] down the composition tree.
 *
 * Why staticCompositionLocalOf (not compositionLocalOf):
 * - Typography never changes at runtime (no dark/light variants, no user switching).
 *   staticCompositionLocalOf has zero snapshot overhead on reads, which is the right
 *   trade-off when the value is effectively constant for the lifetime of the app.
 *
 * Default value: system font (FontFamily.Default) scale used as a fallback outside
 * a [TravelMonkTheme] wrapper. When a custom font is introduced, update the default
 * here and it will propagate everywhere automatically.
 */
val LocalTravelMonkTypography = staticCompositionLocalOf {
    TravelMonkTypography(
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        caption = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}
