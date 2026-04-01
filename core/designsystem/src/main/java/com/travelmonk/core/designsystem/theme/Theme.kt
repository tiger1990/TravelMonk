package com.travelmonk.core.designsystem.theme

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
import com.travelmonk.core.designsystem.color.BackgroundDark
import com.travelmonk.core.designsystem.color.BackgroundLight
import com.travelmonk.core.designsystem.color.ErrorRed
import com.travelmonk.core.designsystem.color.LocalTravelMonkColors
import com.travelmonk.core.designsystem.color.SurfaceDark
import com.travelmonk.core.designsystem.color.SurfaceLight
import com.travelmonk.core.designsystem.color.TravelBlue
import com.travelmonk.core.designsystem.color.TravelGreen
import com.travelmonk.core.designsystem.color.TravelMonkColors
import com.travelmonk.core.designsystem.color.TravelOrange
import com.travelmonk.core.designsystem.color.GrayText
import com.travelmonk.core.designsystem.color.OnBackgroundDark
import com.travelmonk.core.designsystem.color.OnBackgroundLight
import com.travelmonk.core.designsystem.shapes.LocalRadius
import com.travelmonk.core.designsystem.shapes.Radius
import com.travelmonk.core.designsystem.sizing.Dimensions
import com.travelmonk.core.designsystem.sizing.LocalDimensions
import com.travelmonk.core.designsystem.spacing.LocalSpacing
import com.travelmonk.core.designsystem.spacing.Spacing
import com.travelmonk.core.designsystem.typography.LocalTravelMonkTypography
import com.travelmonk.core.designsystem.typography.TravelMonkTypography

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
    // Defaulting to false to preserve brand identity as per architecture guidelines
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
            window.statusBarColor = customColors.primary.toArgb()
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
