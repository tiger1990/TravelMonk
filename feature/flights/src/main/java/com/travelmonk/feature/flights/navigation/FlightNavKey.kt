package com.travelmonk.feature.flights.navigation

import com.travelmonk.core.navigation.TravelNavKey

sealed interface FlightNavKey : TravelNavKey {
    data object Search : FlightNavKey
    data class Results(val from: String, val to: String) : FlightNavKey
}
