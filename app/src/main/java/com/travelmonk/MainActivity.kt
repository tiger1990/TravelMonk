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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.common.config.FeatureFlagStore
import com.travelmonk.core.ui.flags.LocalFeatureFlags
import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.core.navigation.NavEntryInstallerSet
import com.travelmonk.feature.onboarding.data.local.UserSessionStore
import com.travelmonk.feature.onboarding.domain.model.AuthState
import com.travelmonk.feature.onboarding.navigation.OnboardingNavigationBus
import com.travelmonk.feature.onboardingapi.navigator.OnboardingNavigator
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.navigation.NavigationRegistry
import com.travelmonk.ui.OnboardingFlow
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
    lateinit var featureFlagStore: FeatureFlagStore

    @Inject
    lateinit var userSessionStore: UserSessionStore

    @Inject
    lateinit var onboardingNavigationBus: OnboardingNavigationBus

    @Inject
    lateinit var onboardingNavigator: OnboardingNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val flags by featureFlagStore.flagsFlow.collectAsStateWithLifecycle()

            TravelMonkTheme {
                CompositionLocalProvider(LocalFeatureFlags provides flags) {
                    val authState by userSessionStore.authStateFlow.collectAsStateWithLifecycle()

                    // Track custom splash completion separately from auth resolution
                    var splashComplete by rememberSaveable { mutableStateOf(false) }
                    var isSystemSplashMoving by rememberSaveable { mutableStateOf(true) }

                    // Keep system splash until both auth has resolved AND our Lottie animation is done
                    splashScreen.setKeepOnScreenCondition {
                        isSystemSplashMoving || authState is AuthState.Loading
                    }

                    // Show splash until auth resolves AND animation is done
                    val showContent = splashComplete && authState !is AuthState.Loading

                    AnimatedContent(
                        targetState = when {
                            !showContent -> "splash"
                            authState is AuthState.Authenticated -> "main"
                            else -> "onboarding"
                        },
                        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(300)) },
                        label = "auth_gate"
                    ) { target ->
                        when (target) {
                            "splash" -> TravelMonkSplashScreen(
                                onReady = { isSystemSplashMoving = false },
                                onAnimationComplete = { splashComplete = true }
                            )

                            "onboarding" -> OnboardingFlow(
                                bus = onboardingNavigationBus,
                                navigator = onboardingNavigator
                            )

                            else -> TravelMonkApp(
                                globalNavigator = globalNavigator,
                                registry = navigationRegistry,
                                navEntryInstallers = NavEntryInstallerSet(navEntryInstallers)
                            )
                        }
                    }
                }
            }
        }
    }
}
