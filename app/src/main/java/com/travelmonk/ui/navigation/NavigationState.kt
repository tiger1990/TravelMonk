package com.travelmonk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.bookings.navigation.BookingNavKey
import com.travelmonk.feature.experiences.navigation.ExperienceNavKey
import com.travelmonk.feature.home.navigation.HomeNavKey
import com.travelmonk.feature.services.navigation.ServiceNavKey
import com.travelmonk.feature.stays.navigation.StayNavKey
import com.travelmonk.feature.transport.navigation.TransportNavKey

class NavigationState {
    // One isolated back stack per bottom-bar tab — preserves navigation history on tab switch
    private val homeStack = mutableStateListOf<TravelNavKey>(HomeNavKey.Root)
    private val transportStack = mutableStateListOf<TravelNavKey>(TransportNavKey.Root)
    private val staysStack = mutableStateListOf<TravelNavKey>(StayNavKey.Search)
    private val experiencesStack = mutableStateListOf<TravelNavKey>(ExperienceNavKey.Root)
    private val servicesStack = mutableStateListOf<TravelNavKey>(ServiceNavKey.Root)
    private val bookingsStack = mutableStateListOf<TravelNavKey>(BookingNavKey.Root)

    var currentTab by mutableStateOf(BottomBarItem.Home as BottomBarItem)
        private set

    val backStack: List<TravelNavKey> get() = when (currentTab) {
        BottomBarItem.Home        -> homeStack
        BottomBarItem.Transport   -> transportStack
        BottomBarItem.Stays       -> staysStack
        BottomBarItem.Experiences -> experiencesStack
        BottomBarItem.Services    -> servicesStack
        BottomBarItem.Bookings    -> bookingsStack
    }

    fun selectTab(item: BottomBarItem) {
        currentTab = item
    }

    fun navigateTo(key: TravelNavKey) {
        (backStack as MutableList<TravelNavKey>).add(key)
    }

    fun pop() {
        if (backStack.size > 1) (backStack as MutableList<TravelNavKey>).removeLastOrNull()
    }
}

@Composable
fun rememberNavigationState(): NavigationState {
    return remember { NavigationState() }
}
