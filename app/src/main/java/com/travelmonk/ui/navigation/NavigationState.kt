package com.travelmonk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey
import com.travelmonk.feature.homeapi.navigation.HomeNavKey
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.transportapi.navigation.TransportNavKey
import com.travelmonk.navigation.NavigationRegistry

class NavigationState(private val registry: NavigationRegistry) {

    // One isolated back stack per bottom-bar tab — preserves navigation history on tab switch
    private val homeStack        = mutableStateListOf<TravelNavKey>(HomeNavKey.Root)
    private val transportStack   = mutableStateListOf<TravelNavKey>(TransportNavKey.Root)
    private val staysStack       = mutableStateListOf<TravelNavKey>(StayNavKey.Search)
    private val experiencesStack = mutableStateListOf<TravelNavKey>(ExperienceNavKey.Root)
    private val servicesStack    = mutableStateListOf<TravelNavKey>(ServiceNavKey.Root)
    private val bookingsStack    = mutableStateListOf<TravelNavKey>(BookingNavKey.Root)

    var currentTab by mutableStateOf(BottomBarItem.Home as BottomBarItem)
        private set

    val backStack: List<TravelNavKey> get() = currentMutableStack()

    private fun currentMutableStack(): SnapshotStateList<TravelNavKey> = when (currentTab) {
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

    /**
     * Navigates to [key] by:
     *  1. Asking the registry which tab owns this key (via NavTab)
     *  2. Switching to that tab if different from the current one
     *  3. Pushing the key onto the (now active) tab's back stack
     *
     * If no handler is registered for the key, the key is pushed onto
     * the current tab's stack as a safe fallback.
     */
    fun navigateTo(key: TravelNavKey) {
        val destination = registry.resolve(key)
        if (destination != null) {
            val targetTab = destination.navTab.toBottomBarItem()
            if (currentTab != targetTab) currentTab = targetTab
        }
        val stack = currentMutableStack()
        if (stack.lastOrNull() != key) stack.add(key)
    }

    fun pop() {
        val stack = currentMutableStack()
        if (stack.size > 1) stack.removeLastOrNull()
    }

    // App-layer mapping: NavTab enum → BottomBarItem. Lives here because BottomBarItem
    // is an app type. Adding a new tab requires updating both NavTab and this function.
    private fun NavTab.toBottomBarItem(): BottomBarItem = when (this) {
        NavTab.HOME        -> BottomBarItem.Home
        NavTab.TRANSPORT   -> BottomBarItem.Transport
        NavTab.STAYS       -> BottomBarItem.Stays
        NavTab.EXPERIENCES -> BottomBarItem.Experiences
        NavTab.SERVICES    -> BottomBarItem.Services
        NavTab.BOOKINGS    -> BottomBarItem.Bookings
    }
}

@Composable
fun rememberNavigationState(registry: NavigationRegistry): NavigationState {
    return remember { NavigationState(registry) }
}
