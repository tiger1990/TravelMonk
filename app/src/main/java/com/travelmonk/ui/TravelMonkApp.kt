package com.travelmonk.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation3.ui.NavDisplay
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator
import com.travelmonk.feature.homeapi.navigator.HomeNavigator
import com.travelmonk.feature.servicesapi.navigator.ServiceNavigator
import com.travelmonk.feature.staysapi.navigator.StayNavigator
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.navigation.NavCommand
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

    // Listen to navigation events from the GlobalNavigator
    LaunchedEffect(navigationState, globalNavigator) {
        globalNavigator.navEvents.collect { command ->
            when (command) {
                is NavCommand.Navigate -> navigationState.navigateTo(command.key)
                is NavCommand.Back -> navigationState.pop()
            }
        }
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
            transitionSpec = {
                (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(300)))
            },
            popTransitionSpec = {
                (slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300)))
            }
        )
    }
}
