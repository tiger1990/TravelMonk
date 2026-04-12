package com.travelmonk.feature.bookingsapi.navigator

import androidx.compose.runtime.Stable

@Stable // All implementations are Hilt singletons; safe for Compose to skip recomposition
interface BookingNavigator {
    fun back()
    fun navigateToMyBookings()
}
