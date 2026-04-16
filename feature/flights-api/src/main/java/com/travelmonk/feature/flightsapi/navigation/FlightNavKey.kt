package com.travelmonk.feature.flightsapi.navigation

import com.travelmonk.core.navigation.TravelNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface FlightNavKey : TravelNavKey {
    @Serializable
    @SerialName("flight.search")
    data object Search : FlightNavKey

    @Serializable
    @SerialName("flight.results")
    data class Results(val from: String, val to: String) : FlightNavKey
}
