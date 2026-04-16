package com.travelmonk.core.design.system.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.travelmonk.core.design.system.color.BackgroundDark
import com.travelmonk.core.design.system.color.BackgroundLight
import com.travelmonk.core.design.system.color.ErrorRed
import com.travelmonk.core.design.system.color.LocalTravelMonkColors
import com.travelmonk.core.design.system.color.SurfaceDark
import com.travelmonk.core.design.system.color.SurfaceLight
import com.travelmonk.core.design.system.color.TravelBlue
import com.travelmonk.core.design.system.color.TravelGreen
import com.travelmonk.core.design.system.color.TravelMonkColors
import com.travelmonk.core.design.system.color.TravelOrange
import com.travelmonk.core.design.system.color.GrayText
import com.travelmonk.core.design.system.color.OnBackgroundDark
import com.travelmonk.core.design.system.color.OnBackgroundLight
import com.travelmonk.core.design.system.shapes.LocalRadius
import com.travelmonk.core.design.system.shapes.Radius
import com.travelmonk.core.design.system.sizing.Dimensions
import com.travelmonk.core.design.system.sizing.LocalDimensions
import com.travelmonk.core.design.system.spacing.LocalSpacing
import com.travelmonk.core.design.system.spacing.Spacing
import com.travelmonk.core.design.system.typography.LocalTravelMonkTypography
import com.travelmonk.core.design.system.typography.TravelMonkTypography

private val DarkColorScheme = darkColorScheme(
    primary = TravelOrange,
    secondary = TravelBlue,
    tertiary = TravelGreen,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TravelOrange,
    secondary = TravelBlue,
    tertiary = TravelGreen,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun TravelMonkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Defaulting too false to preserve brand identity as per architecture guidelines
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val customColors = TravelMonkColors(
        primary = TravelOrange,
        secondary = TravelBlue,
        tertiary = TravelGreen,
        background = if (darkTheme) BackgroundDark else BackgroundLight,
        surface = if (darkTheme) SurfaceDark else SurfaceLight,
        error = ErrorRed,
        onPrimary = Color.White,
        onSecondary = if (darkTheme) Color.White else Color.Black,
        onBackground = if (darkTheme) OnBackgroundDark else OnBackgroundLight,
        onSurface = if (darkTheme) OnBackgroundDark else OnBackgroundLight,
        grayText = GrayText,
        isLight = !darkTheme
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // With edgeToEdge enabled in MainActivity, we just need to control icon contrast.
            // The status bar color itself is handled by the window background/Surface.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalTravelMonkColors provides customColors,
        LocalTravelMonkTypography provides LocalTravelMonkTypography.current,
        LocalSpacing provides LocalSpacing.current,
        LocalDimensions provides LocalDimensions.current,
        LocalRadius provides LocalRadius.current
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
