package com.travelmonk.ui.navigation

import androidx.annotation.DrawableRes
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.homeapi.navigation.HomeNavKey
import com.travelmonk.feature.transportapi.navigation.TransportNavKey
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey

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
