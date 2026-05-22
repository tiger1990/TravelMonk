package com.travelmonk.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.homeapi.navigation.HomeNavKey
import com.travelmonk.feature.transportapi.navigation.TransportNavKey
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey

/**
 * Represents a top-level destination in the bottom navigation bar.
 * Marked as @Immutable to ensure the BottomBar is skippable during recomposition.
 *
 * [navTab] links this item to its [NavTab] identity so NavigationState can resolve
 * tab roots without importing feature nav keys directly.
 */
@Immutable
sealed class BottomBarItem(
    val route: TravelNavKey,
    val navTab: NavTab,
    val title: String,
    @param:DrawableRes val icon: Int
) {
    data object Home : BottomBarItem(HomeNavKey.Root, NavTab.HOME, "Home", TravelMonkIcons.Home)
    data object Transport : BottomBarItem(TransportNavKey.Root, NavTab.TRANSPORT, "Transport", TravelMonkIcons.DirectionsTransit)
    data object Stays : BottomBarItem(StayNavKey.Search, NavTab.STAYS, "Stays", TravelMonkIcons.Hotel)
    data object Experiences : BottomBarItem(ExperienceNavKey.Root, NavTab.EXPERIENCES, "Experiences", TravelMonkIcons.Explore)
    data object Services : BottomBarItem(ServiceNavKey.Root, NavTab.SERVICES, "Services", TravelMonkIcons.CleaningServices)
    data object Bookings : BottomBarItem(BookingNavKey.Root, NavTab.BOOKINGS, "Bookings", TravelMonkIcons.ConfirmationNumber)

    companion object {
        val all: List<BottomBarItem> by lazy { listOf(Home, Transport, Stays, Experiences, Services, Bookings) }
    }
}
