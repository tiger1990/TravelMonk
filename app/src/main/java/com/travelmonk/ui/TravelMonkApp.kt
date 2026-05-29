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
import com.travelmonk.core.ui.LocalNavContentPadding
import com.travelmonk.core.ui.flags.LocalFeatureFlags
import com.travelmonk.core.navigation.NavEntryInstallerSet
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.navigation.NavigationRegistry
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.ui.navigation.*

@Composable
fun TravelMonkApp(
    globalNavigator: GlobalNavigator,
    registry: NavigationRegistry,
    navEntryInstallers: NavEntryInstallerSet
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

    // Access flags via CompositionLocal
    val flags = LocalFeatureFlags.current

    val bottomBarItems = remember(flags) {
        BottomBarItems(
            buildList {
                add(BottomBarItem.Home)
                if (flags.isTransportEnabled) add(BottomBarItem.Transport)
                if (flags.isStaysEnabled) add(BottomBarItem.Stays)
                if (flags.isExperiencesEnabled) add(BottomBarItem.Experiences)
                if (flags.isServicesEnabled) add(BottomBarItem.Services)
                add(BottomBarItem.Bookings)
            }
        )
    }

    Surface(
        color = TravelMonkTheme.colors.primary
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                val currentKey = navigationState.activeBackStack.lastOrNull()
                // Show bottom bar only when on a root tab destination
                val isTopLevel = bottomBarItems.items.any { it.route == currentKey }

                if (isTopLevel) {
                    TravelMonkBottomBar(
                        items = bottomBarItems,
                        selectedItem = navigationState.currentTab,
                        onItemSelected = navigationState::selectTab
                    )
                }
            }
        ) { innerPadding ->
            CompositionLocalProvider(LocalNavContentPadding provides innerPadding.calculateBottomPadding()) {
            NavDisplay(
                entries = navigationState.toDecoratedEntries(entryProvider),
                // To achieve the glass effect, we ignore bottom padding so content flows behind the bar.
                // We keep top padding for the status/top bar area.
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                onBack = { navigationState.pop() },
                // Tab switches (tapping a bottom bar item) should cross-fade — not slide.
                // Sliding is only correct for forward push-navigation within a tab.
                // We detect a tab switch by checking if the incoming top key is a root tab destination.
                transitionSpec = {
                    val toKey = targetState.key
                    val isTabSwitch = bottomBarItems.items.any { it.route == toKey }
                    if (isTabSwitch) {
                        // Peer-level transition: fade out old tab, fade in new tab
                        fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                    } else {
                        // Forward push within a tab: slide in from right
                        (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(300)))
                    }
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
            } // CompositionLocalProvider
        }
    }
}
