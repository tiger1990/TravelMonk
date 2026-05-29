package com.travelmonk.feature.flights.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.flights.domain.model.Flight
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class FlightSearchState(
    val fromCity: String = "San Francisco",
    val fromCode: String = "SFO",
    val toCity: String = "New York",
    val toCode: String = "JFK",
    val departureDate: String = "Oct 24, 2024",
    val passengers: Int = 1,
    val tripType: TripType = TripType.ONE_WAY,
    val isLoading: Boolean = false,
    val flights: ImmutableList<Flight> = persistentListOf(),
    val error: String? = null
) : UiState

enum class TripType { ONE_WAY, ROUND_TRIP, MULTI_CITY }

sealed interface FlightIntent : UiIntent {
    data class ChangeTripType(val type: TripType) : FlightIntent
    data class SwapCities(val from: String, val to: String) : FlightIntent
    data object SearchFlights : FlightIntent
    data class LoadResults(val from: String, val to: String) : FlightIntent
}

sealed interface FlightEffect : UiEffect {
    data class NavigateToResults(val from: String, val to: String) : FlightEffect
    data class ShowError(val message: String) : FlightEffect
}
