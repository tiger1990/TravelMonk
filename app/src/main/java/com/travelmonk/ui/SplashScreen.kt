package com.travelmonk.ui

import android.content.res.Configuration
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.travelmonk.R
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import kotlinx.coroutines.delay

// How long the branded splash is shown before handing off to the app.
private const val SPLASH_DISPLAY_MILLIS = 1200L

@Composable
fun TravelMonkSplashScreen(
    onReady: () -> Unit,
    onAnimationComplete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isPreview = LocalInspectionMode.current

    // Drive the reveal animation off a single flag instead of a Lottie composition.
    var started by remember { mutableStateOf(isPreview) }

    val contentAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "splash_content_alpha"
    )

    val contentScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.92f,
        animationSpec = tween(durationMillis = 700, easing = EaseOutCubic),
        label = "splash_content_scale"
    )

    // No external asset to load — signal ready immediately so the system splash hands off,
    // start the reveal, then complete after the branded splash has been shown.
    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect
        onReady()
        started = true
        delay(SPLASH_DISPLAY_MILLIS)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) TravelMonkTheme.colors.background else TravelMonkTheme.colors.secondary)
    ) {
        // Centered brand mark
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    alpha = contentAlpha
                    scaleX = contentScale
                    scaleY = contentScale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = TravelMonkTheme.typography.headlineLarge.copy(
                    color = TravelMonkTheme.colors.primary,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        // Bottom branding information
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TravelMonkTheme.spacing.extraLarge)
                .graphicsLayer { alpha = contentAlpha },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YOUR JOURNEY STARTS HERE",
                style = TravelMonkTheme.typography.labelMedium.copy(
                    color = TravelMonkTheme.colors.onBackground.copy(alpha = 0.6f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Preview(name = "Splash – Light", showBackground = true)
@Preview(name = "Splash – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TravelMonkSplashScreenPreview() {
    TravelMonkTheme {
        TravelMonkSplashScreen(onReady = {}, onAnimationComplete = {})
    }
}
