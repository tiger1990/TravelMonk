package com.travelmonk.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation3.ui.NavDisplay
import com.travelmonk.feature.flights.navigator.FlightNavigator
import com.travelmonk.feature.stays.navigator.StayNavigator
import com.travelmonk.feature.services.navigator.ServiceNavigator
import com.travelmonk.feature.experiences.navigator.ExperienceNavigator
import com.travelmonk.feature.home.navigator.HomeNavigator
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.ui.navigation.*

@Composable
fun TravelMonkApp(
    globalNavigator: GlobalNavigator,
    homeNavigator: HomeNavigator,
    flightNavigator: FlightNavigator,
    stayNavigator: StayNavigator,
    serviceNavigator: ServiceNavigator,
    experienceNavigator: ExperienceNavigator
) {
    val navigationState = rememberNavigationState()

    /**
     * UseCase:
     * Register/unregister listeners
     * Subscribe/unsubscribe to streams
     * Add/remove observers
     * Attach/detach callbacks
     * Work with external APIs that need cleanup
     *
     * DisposableEffect(lifecycleOwner) {
     *     val observer = LifecycleEventObserver { _, event ->
     *         // handle lifecycle events
     *     }
     *
     *     lifecycleOwner.lifecycle.addObserver(observer)
     *
     *     onDispose {
     *         lifecycleOwner.lifecycle.removeObserver(observer)
     *     }
     * }
     * Whenever navigationState changes(key should represent what the effect depends on),
     * the DisposableEffect block will be executed (restart the side effect).
     * But before restarting, clean up the old one.
     */
    DisposableEffect(navigationState) {
        globalNavigator.bind(navigationState)
        onDispose { globalNavigator.unbind() }
    }

    val entryProvider = remember {
        provideTravelEntryProvider(
            homeNavigator = homeNavigator,
            flightNavigator = flightNavigator,
            stayNavigator = stayNavigator,
            serviceNavigator = serviceNavigator,
            experienceNavigator = experienceNavigator
        )
    }

    val bottomBarItems = listOf(
        BottomBarItem.Home,
        BottomBarItem.Transport,
        BottomBarItem.Stays,
        BottomBarItem.Experiences,
        BottomBarItem.Services,
        BottomBarItem.Bookings
    )

    Scaffold(
        bottomBar = {
            val currentStack = navigationState.backStack
            val currentKey = currentStack.lastOrNull()
            // Show bottom bar only when on a root tab destination
            val isTopLevel = bottomBarItems.any { it.route == currentKey }

            if (isTopLevel) {
                NavigationBar {
                    bottomBarItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(item.icon),
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = navigationState.currentTab == item,
                            onClick = {
                                navigationState.selectTab(item)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = navigationState.backStack,
            onBack = { navigationState.pop() },
            entryProvider = entryProvider,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
