package com.travelmonk.ui.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator
import com.travelmonk.feature.flightsapi.navigation.FlightNavKey
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator
import com.travelmonk.feature.homeapi.navigation.HomeNavKey
import com.travelmonk.feature.homeapi.navigator.HomeNavigator
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey
import com.travelmonk.feature.servicesapi.navigator.ServiceNavigator
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.staysapi.navigator.StayNavigator
import com.travelmonk.feature.transportapi.navigation.TransportNavKey
import com.travelmonk.feature.bookings.ui.BookingConfirmationScreen
import com.travelmonk.feature.bookings.ui.MyBookingsScreen
import com.travelmonk.feature.experiences.ui.ExperiencesScreen
import com.travelmonk.feature.flights.ui.FlightResultsScreen
import com.travelmonk.feature.home.ui.HomeScreen
import com.travelmonk.feature.services.ui.ServicesScreen
import com.travelmonk.feature.stays.ui.StaySearchScreen
import com.travelmonk.feature.transport.ui.TransportScreen

fun provideTravelEntryProvider(
    homeNavigator: HomeNavigator,
    flightNavigator: FlightNavigator,
    stayNavigator: StayNavigator,
    serviceNavigator: ServiceNavigator,
    experienceNavigator: ExperienceNavigator
): (TravelNavKey) -> NavEntry<TravelNavKey> = entryProvider {

    entry<HomeNavKey.Root> {
        HomeScreen(navigator = homeNavigator)
    }

    entry<TransportNavKey.Root> {
        TransportScreen()
    }

    entry<StayNavKey.Search> {
        StaySearchScreen(navigator = stayNavigator)
    }

    entry<ExperienceNavKey.Root> {
        ExperiencesScreen(navigator = experienceNavigator)
    }

    entry<ServiceNavKey.Root> {
        ServicesScreen(navigator = serviceNavigator)
    }

    entry<BookingNavKey.Root> {
        MyBookingsScreen()
    }

    entry<FlightNavKey.Results> { key ->
        FlightResultsScreen(
            from = key.from,
            to = key.to,
            navigator = flightNavigator,
            onBook = { stayNavigator.back() /* Placeholder for booking flow */ }
        )
    }

    entry<BookingNavKey.Confirmation> { key ->
        BookingConfirmationScreen(
            type = key.type,
            title = key.title,
            onDone = { /* Navigate to bookings */ }
        )
    }
}
