package com.travelmonk.feature.flightsapi.navigator

import androidx.compose.runtime.Stable
import com.travelmonk.core.model.BookingType
import com.travelmonk.feature.flightsapi.navigation.FlightNavKey

@Stable // All implementations are Hilt singletons; safe for Compose to skip recomposition
interface FlightNavigator {
    fun navigateTo(key: FlightNavKey)
    fun back()
    fun navigateToBookingConfirmation(type: BookingType, title: String)
}
