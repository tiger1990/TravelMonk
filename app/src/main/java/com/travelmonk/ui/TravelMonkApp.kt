package com.travelmonk.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.travelmonk.core.common.config.FeatureFlags
import com.travelmonk.core.navigation.NavEntryInstallerSet
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.navigation.NavigationRegistry
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.ui.navigation.*

@Composable
fun TravelMonkApp(
    globalNavigator: GlobalNavigator,
    registry: NavigationRegistry,
    navEntryInstallers: NavEntryInstallerSet,
    featureFlags: FeatureFlags
) {
    val navigationState = rememberNavigationState(registry, globalNavigator)

    val entryProvider = remember(navEntryInstallers) {
        entryProvider {
            navEntryInstallers.installers.forEach { installer ->
                with(installer) {
                    install()
                }
            }
        }
    }

    val bottomBarItems = buildList {
        add(BottomBarItem.Home)
        if (featureFlags.isTransportEnabled) add(BottomBarItem.Transport)
        if (featureFlags.isStaysEnabled) add(BottomBarItem.Stays)
        if (featureFlags.isExperiencesEnabled) add(BottomBarItem.Experiences)
        if (featureFlags.isServicesEnabled) add(BottomBarItem.Services)
        add(BottomBarItem.Bookings)
    }

    Surface(
        color = TravelMonkTheme.colors.primary
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                val currentKey = navigationState.activeBackStack.lastOrNull()
                // Show bottom bar only when on a root tab destination
                val isTopLevel = bottomBarItems.any { it.route == currentKey }

                if (isTopLevel) {
                    TravelMonkBottomBar(
                        items = bottomBarItems,
                        selectedItem = navigationState.currentTab,
                        onItemSelected = navigationState::selectTab
                    )
                }
            }
        ) { innerPadding ->
            NavDisplay(
                entries = navigationState.toDecoratedEntries(entryProvider),
                modifier = Modifier.padding(innerPadding),
                onBack = { navigationState.pop() },
                transitionSpec = {
                    (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))) togetherWith
                            (slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(300)))
                },
                popTransitionSpec = {
                    (slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300))) togetherWith
                            (slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300)))
                },
                predictivePopTransitionSpec = {
                    (slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300))) togetherWith
                            (slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300)))
                }
                // try this: https://proandroiddev.com/nested-routes-with-navigation-3-af0cd8223986
            )
        }
    }
}
