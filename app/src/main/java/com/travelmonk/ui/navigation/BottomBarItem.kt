package com.travelmonk.ui.navigation

import androidx.annotation.DrawableRes
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.home.navigation.HomeNavKey
import com.travelmonk.feature.transport.navigation.TransportNavKey
import com.travelmonk.feature.stays.navigation.StayNavKey
import com.travelmonk.feature.experiences.navigation.ExperienceNavKey
import com.travelmonk.feature.bookings.navigation.BookingNavKey
import com.travelmonk.feature.services.navigation.ServiceNavKey

sealed class BottomBarItem(
    val route: TravelNavKey,
    val title: String,
    @param:DrawableRes val icon: Int
) {
    data object Home : BottomBarItem(HomeNavKey.Root, "Home", TravelMonkIcons.Home)
    data object Transport : BottomBarItem(TransportNavKey.Root, "Transport", TravelMonkIcons.DirectionsTransit)
    data object Stays : BottomBarItem(StayNavKey.Search, "Stays", TravelMonkIcons.Hotel)
    data object Experiences : BottomBarItem(ExperienceNavKey.Root, "Experiences", TravelMonkIcons.Explore)
    data object Services : BottomBarItem(ServiceNavKey.Root, "Services", TravelMonkIcons.CleaningServices)
    data object Bookings : BottomBarItem(BookingNavKey.Root, "Bookings", TravelMonkIcons.ConfirmationNumber)
}
