package com.travelmonk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.common.config.FeatureFlags
import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.core.navigation.NavEntryInstallerSet
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.navigation.NavigationRegistry
import com.travelmonk.ui.TravelMonkApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // GlobalNavigator is the NavigationBus — needed to bind/unbind the composition-scoped NavigationState
    @Inject lateinit var globalNavigator: GlobalNavigator
    @Inject lateinit var navigationRegistry: NavigationRegistry

    // Each feature module contributes its own NavEntryInstaller via @IntoSet into ActivityRetainedComponent
    @Inject lateinit var navEntryInstallers: Set<@JvmSuppressWildcards NavEntryInstaller>
    @Inject lateinit var featureFlags: FeatureFlags

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelMonkTheme {
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
