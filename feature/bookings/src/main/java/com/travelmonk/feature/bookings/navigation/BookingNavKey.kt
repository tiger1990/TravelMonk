package com.travelmonk.feature.bookings.navigation

import com.travelmonk.core.navigation.TravelNavKey

sealed interface BookingNavKey : TravelNavKey {
    data object Root : BookingNavKey
    data class Confirmation(val type: String, val title: String) : BookingNavKey
}
