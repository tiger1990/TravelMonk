package com.travelmonk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.common.config.FeatureFlags
import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.core.navigation.NavEntryInstallerSet
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.navigation.NavigationRegistry
import com.travelmonk.ui.TravelMonkApp
import com.travelmonk.ui.TravelMonkSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var globalNavigator: GlobalNavigator

    @Inject
    lateinit var navigationRegistry: NavigationRegistry

    @Inject
    lateinit var navEntryInstallers: Set<@JvmSuppressWildcards NavEntryInstaller>

    @Inject
    lateinit var featureFlags: FeatureFlags

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            TravelMonkTheme {
                // Persist 'showMainApp' across config changes (rotations)
                var showMainApp by rememberSaveable { mutableStateOf(false) }

                // Track system splash visibility locally in composition
                var isSystemSplashMoving by remember { mutableStateOf(true) }

                // Only keep system splash visible if we haven't shown the main app yet
                splashScreen.setKeepOnScreenCondition { isSystemSplashMoving && !showMainApp }

                AnimatedContent(
                    targetState = showMainApp, transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                    }, label = "splash_to_app"
                ) { targetShowMainApp ->
                    if (!targetShowMainApp) {
                        TravelMonkSplashScreen(
                            onReady = { isSystemSplashMoving = false },
                            onAnimationComplete = { showMainApp = true })
                    } else {
                        TravelMonkApp(
                            globalNavigator = globalNavigator,
                            registry = navigationRegistry,
                            navEntryInstallers = NavEntryInstallerSet(navEntryInstallers),
                            featureFlags = featureFlags
                        )
                    }
                }
            }
        }
    }
}
