package com.travelmonk.core.design.system.color

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- 1. Brand Palette (Raw Colors) ---
val PrimaryOrange = Color(0xFFFF5722)
val SecondaryTonal = Color(0xFFEFF2E4)
val TertiaryMuted = Color(0xFF70777A)
val NeutralAnchor = Color(0xFF121212)
val TravelYellow = Color(0xFFFFC107)

// --- 2. Neutral Palette (Light/Dark Surface pairs) ---
val BackgroundLight = Color(0xFFEFF2E4) //Color(0xFFF8F9FB)
val BackgroundDark = Color(0xFF121212)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)
val OnBackgroundLight = Color(0xFF1C1B1F)
val OnBackgroundDark = Color(0xFFE1E3E0)

// Semantic variants
val OnSurfaceVariantLight = Color(0xFF8E8E93)
val OnSurfaceVariantDark = Color(0xFFB0B0B0)
val SurfaceVariantLight = Color(0xFFF1F4F8)
val SurfaceVariantDark = Color(0xFF2D2F36)

// Navigation Specific Colors (Semantic Tokens)
val BottomBarBackgroundLight = SurfaceVariantDark.copy(alpha = 0.7f) // More transparent glass
val BottomBarIndicatorLight = PrimaryOrange
val BottomBarIndicatorContentLight = SecondaryTonal
val BottomBarBackgroundDark = SurfaceVariantDark.copy(alpha = 0.6f) // More transparent glass
val BottomBarIndicatorDark = PrimaryOrange
val BottomBarIndicatorContentDark = SecondaryTonal // Dark navy cutout effect

val ErrorRed = Color(0xFFBA1A1A)
val ErrorContainer = Color(0xFFFFDAD6)
val WarningAmber = Color(0xFFFFC107)

// Image overlay tokens — used on banner/card images
val OnImage = Color.White
val ImageScrim = Color(0x4D000000) // Black 30%

/**
 * TravelMonk Semantic Color Contract.
 * Uses @Stable and mutableStateOf to support smooth color animations and
 * ensure the theme is observable by the Compose runtime.
 */
@Stable
class TravelMonkColors(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    tertiary: Color,
    onTertiary: Color,
    tertiaryContainer: Color,
    background: Color,
    onBackground: Color,
    surface: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    surfaceVariant: Color,
    bottomBarBackground: Color,
    bottomBarIndicator: Color,
    bottomBarIndicatorContent: Color,
    error: Color,
    errorContainer: Color,
    onImage: Color,
    imageScrim: Color,
    isLight: Boolean
) {
    var primary by mutableStateOf(primary)
        private set
    var onPrimary by mutableStateOf(onPrimary)
        private set
    var primaryContainer by mutableStateOf(primaryContainer)
        private set
    var secondary by mutableStateOf(secondary)
        private set
    var onSecondary by mutableStateOf(onSecondary)
        private set
    var secondaryContainer by mutableStateOf(secondaryContainer)
        private set
    var tertiary by mutableStateOf(tertiary)
        private set
    var onTertiary by mutableStateOf(onTertiary)
        private set
    var tertiaryContainer by mutableStateOf(tertiaryContainer)
        private set
    var background by mutableStateOf(background)
        private set
    var onBackground by mutableStateOf(onBackground)
        private set
    var surface by mutableStateOf(surface)
        private set
    var onSurface by mutableStateOf(onSurface)
        private set
    var onSurfaceVariant by mutableStateOf(onSurfaceVariant)
        private set
    var surfaceVariant by mutableStateOf(surfaceVariant)
        private set
    var bottomBarBackground by mutableStateOf(bottomBarBackground)
        private set
    var bottomBarIndicator by mutableStateOf(bottomBarIndicator)
        private set
    var bottomBarIndicatorContent by mutableStateOf(bottomBarIndicatorContent)
        private set
    var error by mutableStateOf(error)
        private set
    var errorContainer by mutableStateOf(errorContainer)
        private set
    var onImage by mutableStateOf(onImage)
        private set
    var imageScrim by mutableStateOf(imageScrim)
        private set
    var isLight by mutableStateOf(isLight)
        private set

    fun updateColorsFrom(other: TravelMonkColors) {
        primary = other.primary
        onPrimary = other.onPrimary
        primaryContainer = other.primaryContainer
        secondary = other.secondary
        onSecondary = other.onSecondary
        secondaryContainer = other.secondaryContainer
        tertiary = other.tertiary
        onTertiary = other.onTertiary
        tertiaryContainer = other.tertiaryContainer
        background = other.background
        onBackground = other.onBackground
        surface = other.surface
        onSurface = other.onSurface
        onSurfaceVariant = other.onSurfaceVariant
        surfaceVariant = other.surfaceVariant
        bottomBarBackground = other.bottomBarBackground
        bottomBarIndicator = other.bottomBarIndicator
        bottomBarIndicatorContent = other.bottomBarIndicatorContent
        error = other.error
        errorContainer = other.errorContainer
        onImage = other.onImage
        imageScrim = other.imageScrim
        isLight = other.isLight
    }

    fun copy(): TravelMonkColors = TravelMonkColors(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        onSurfaceVariant = onSurfaceVariant,
        surfaceVariant = surfaceVariant,
        bottomBarBackground = bottomBarBackground,
        bottomBarIndicator = bottomBarIndicator,
        bottomBarIndicatorContent = bottomBarIndicatorContent,
        error = error,
        errorContainer = errorContainer,
        onImage = onImage,
        imageScrim = imageScrim,
        isLight = isLight
    )
}

val TravelMonkLightPalette = TravelMonkColors(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrange.copy(alpha = 0.12f),
    secondary = SecondaryTonal,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryTonal.copy(alpha = 0.5f),
    tertiary = TertiaryMuted,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryMuted.copy(alpha = 0.12f),
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnBackgroundLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceVariant = SurfaceVariantLight,
    bottomBarBackground = BottomBarBackgroundLight,
    bottomBarIndicator = BottomBarIndicatorLight,
    bottomBarIndicatorContent = BottomBarIndicatorContentLight,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onImage = OnImage,
    imageScrim = ImageScrim,
    isLight = true
)

val TravelMonkDarkPalette = TravelMonkColors(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrange.copy(alpha = 0.24f),
    secondary = SecondaryTonal,
    onSecondary = Color.White,
    secondaryContainer = BackgroundDark.copy(alpha = 0.2f),
    tertiary = TertiaryMuted,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryMuted.copy(alpha = 0.24f),
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnBackgroundDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceVariant = SurfaceVariantDark,
    bottomBarBackground = BottomBarBackgroundDark,
    bottomBarIndicator = BottomBarIndicatorDark,
    bottomBarIndicatorContent = BottomBarIndicatorContentDark,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    onImage = OnImage,
    imageScrim = ImageScrim,
    isLight = false
)

val LocalTravelMonkColors = staticCompositionLocalOf { TravelMonkLightPalette }
