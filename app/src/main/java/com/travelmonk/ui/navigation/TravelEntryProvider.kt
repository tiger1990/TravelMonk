package com.travelmonk.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import com.travelmonk.core.designsystem.theme.TravelMonkTheme
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.home.navigation.HomeNavKey
import com.travelmonk.feature.home.ui.HomeScreen
import com.travelmonk.feature.home.navigator.HomeNavigator
import com.travelmonk.feature.transport.navigation.TransportNavKey
import com.travelmonk.feature.transport.ui.TransportScreen
import com.travelmonk.feature.stays.navigation.StayNavKey
import com.travelmonk.feature.stays.ui.StaySearchScreen
import com.travelmonk.feature.experiences.navigation.ExperienceNavKey
import com.travelmonk.feature.experiences.ui.ExperiencesScreen
import com.travelmonk.feature.services.navigation.ServiceNavKey
import com.travelmonk.feature.services.ui.ServicesScreen
import com.travelmonk.feature.bookings.navigation.BookingNavKey
import com.travelmonk.feature.bookings.ui.MyBookingsScreen
import com.travelmonk.feature.flights.navigation.FlightNavKey
import com.travelmonk.feature.flights.ui.FlightResultsScreen
import com.travelmonk.feature.flights.navigator.FlightNavigator
import com.travelmonk.feature.stays.navigator.StayNavigator
import com.travelmonk.feature.services.navigator.ServiceNavigator
import com.travelmonk.feature.experiences.navigator.ExperienceNavigator

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
            onBook = { airline -> stayNavigator.back() /* Placeholder for booking flow */ }
        )
    }

    entry<BookingNavKey.Confirmation> { key ->
        BookingConfirmationScreen(type = key.type, title = key.title, onDone = { /* Navigate to bookings */ })
    }
}

@Composable
fun BookingConfirmationScreen(type: String, title: String, onDone: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(TravelMonkTheme.colors.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(TravelMonkIcons.CheckCircle),
                contentDescription = null,
                modifier = Modifier.size(TravelMonkTheme.dimensions.iconExtraLarge),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.medium))
            Text(text = "$type Booked Successfully!", style = TravelMonkTheme.typography.titleLarge)
            Text(text = "Confirmed with $title", style = TravelMonkTheme.typography.bodyLarge, color = TravelMonkTheme.colors.grayText)
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.extraLarge))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = TravelMonkTheme.colors.primary)
            ) {
                Text("View My Bookings", color = TravelMonkTheme.colors.onPrimary)
            }
        }
    }
}
