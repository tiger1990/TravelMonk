package com.travelmonk.core.design.system.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.travelmonk.core.design.system.color.LocalTravelMonkColors
import com.travelmonk.core.design.system.color.TravelMonkColors
import com.travelmonk.core.design.system.color.TravelMonkDarkPalette
import com.travelmonk.core.design.system.color.TravelMonkLightPalette
import com.travelmonk.core.design.system.shapes.LocalRadius
import com.travelmonk.core.design.system.shapes.Radius
import com.travelmonk.core.design.system.sizing.Dimensions
import com.travelmonk.core.design.system.sizing.LocalDimensions
import com.travelmonk.core.design.system.spacing.LocalSpacing
import com.travelmonk.core.design.system.spacing.Spacing
import com.travelmonk.core.design.system.typography.LocalTravelMonkTypography
import com.travelmonk.core.design.system.typography.TravelMonkTypography

/**
 * TravelMonk Theme implementation following Google/Uber/Meta best practices.
 * 
 * Features:
 * 1. Observable State: Smooth color transitions between light/dark modes using mutableStateOf.
 * 2. M3 Synchronization: Custom colors are propagated to Material 3 components for consistency.
 * 3. Content Awareness: LocalContentColor is provided automatically based on the background.
 * 4. Semantic Tokens: Decouples UI from raw hex values for easier rebranding.
 */
@Composable
fun TravelMonkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // 1. Select the palette based on the theme
    val targetColors = if (darkTheme) TravelMonkDarkPalette else TravelMonkLightPalette

    // 2. Synchronize Material 3 ColorScheme with our Custom Colors
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = targetColors.primary,
            onPrimary = targetColors.onPrimary,
            primaryContainer = targetColors.primaryContainer,
            secondary = targetColors.secondary,
            onSecondary = targetColors.onSecondary,
            secondaryContainer = targetColors.secondaryContainer,
            background = targetColors.background,
            onBackground = targetColors.onBackground,
            surface = targetColors.surface,
            onSurface = targetColors.onSurface,
            surfaceVariant = targetColors.surfaceVariant,
            onSurfaceVariant = targetColors.onSurfaceVariant,
            error = targetColors.error,
            errorContainer = targetColors.errorContainer
        )
        else -> lightColorScheme(
            primary = targetColors.primary,
            onPrimary = targetColors.onPrimary,
            primaryContainer = targetColors.primaryContainer,
            secondary = targetColors.secondary,
            onSecondary = targetColors.onSecondary,
            secondaryContainer = targetColors.secondaryContainer,
            background = targetColors.background,
            onBackground = targetColors.onBackground,
            surface = targetColors.surface,
            onSurface = targetColors.onSurface,
            surfaceVariant = targetColors.surfaceVariant,
            onSurfaceVariant = targetColors.onSurfaceVariant,
            error = targetColors.error,
            errorContainer = targetColors.errorContainer
        )
    }

    // 3. Enable smooth color cross-fading using remember and updateColorsFrom
    val rememberedColors = remember { targetColors.copy() }.apply {
        updateColorsFrom(targetColors)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Invert status bar icons based on theme for readability
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalTravelMonkColors provides rememberedColors,
        LocalTravelMonkTypography provides LocalTravelMonkTypography.current,
        LocalSpacing provides LocalSpacing.current,
        LocalDimensions provides LocalDimensions.current,
        LocalRadius provides LocalRadius.current,
        LocalContentColor provides rememberedColors.onBackground
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

object TravelMonkTheme {
    val colors: TravelMonkColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTravelMonkColors.current

    val typography: TravelMonkTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTravelMonkTypography.current

    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val dimensions: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current

    val radius: Radius
        @Composable
        @ReadOnlyComposable
        get() = LocalRadius.current
}
