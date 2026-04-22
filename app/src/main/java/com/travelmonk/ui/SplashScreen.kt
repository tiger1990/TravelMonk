package com.travelmonk.ui

import android.content.res.Configuration
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.travelmonk.R
import com.travelmonk.core.design.system.theme.TravelMonkTheme

@Composable
fun TravelMonkSplashScreen(
    onReady: () -> Unit,
    onAnimationComplete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isPreview = LocalInspectionMode.current
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.travel_splash)
    )
    val lottieState = animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    // Fade in text quickly. In preview, show immediately (alpha 1f).
    val textAlpha by animateFloatAsState(
        targetValue = if (isPreview || composition != null) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "text_fade_in"
    )

    // Multi-layer effect: Slight scale in for the whole content
    val contentScale by animateFloatAsState(
        targetValue = if (composition != null) 1f else 0.95f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "splash_content_scale"
    )

    // Signal ready to hide system splash as soon as the Lottie composition is loaded
    LaunchedEffect(composition) {
        if (composition != null) {
            onReady()
        }
    }

    LaunchedEffect(lottieState.isAtEnd) {
        if (lottieState.isAtEnd) {
            onAnimationComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) TravelMonkTheme.colors.background else TravelMonkTheme.colors.secondary)
    ) {
        // Full-screen Lottie Animation (Immersive)
        LottieAnimation(
            composition = composition,
            progress = { lottieState.progress },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = contentScale
                    scaleY = contentScale
                },
            contentScale = ContentScale.FillBounds
        )

        // Bottom Branding Information
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = TravelMonkTheme.spacing.extraLarge)
                .graphicsLayer { alpha = textAlpha },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = TravelMonkTheme.typography.headlineLarge.copy(
                    color = TravelMonkTheme.colors.primary,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )

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
