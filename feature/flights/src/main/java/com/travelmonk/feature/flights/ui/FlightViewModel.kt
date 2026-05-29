package com.travelmonk.feature.flights.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.flights.domain.usecase.SearchFlightsUseCase
import com.travelmonk.feature.flights.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val searchFlightsUseCase: SearchFlightsUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<FlightSearchState, FlightIntent, FlightEffect>(
    FlightSearchState(
        fromCity      = savedStateHandle[KEY_FROM_CITY]      ?: "San Francisco",
        fromCode      = savedStateHandle[KEY_FROM_CODE]      ?: "SFO",
        toCity        = savedStateHandle[KEY_TO_CITY]        ?: "New York",
        toCode        = savedStateHandle[KEY_TO_CODE]        ?: "JFK",
        departureDate = savedStateHandle[KEY_DEPARTURE_DATE] ?: "Oct 24, 2024",
        passengers    = savedStateHandle[KEY_PASSENGERS]     ?: 1,
        tripType      = savedStateHandle.get<String>(KEY_TRIP_TYPE)
            ?.let { name -> TripType.entries.firstOrNull { it.name == name } }
            ?: TripType.ONE_WAY
    )
) {

    // G4: single job reference cancelled before each new search — prevents double-tap from
    // launching two concurrent use-case calls and firing NavigateToResults twice.
    private var searchJob: Job? = null

    override fun handleIntent(intent: FlightIntent) {
        when (intent) {
            is FlightIntent.ChangeTripType -> {
                setState { copy(tripType = intent.type) }
                savedStateHandle[KEY_TRIP_TYPE] = intent.type.name
            }
            is FlightIntent.SwapCities -> {
                val newFromCity = intent.to
                val newToCity   = intent.from
                val newFromCode = currentState.toCode
                val newToCode   = currentState.fromCode
                setState { copy(fromCity = newFromCity, fromCode = newFromCode, toCity = newToCity, toCode = newToCode) }
                savedStateHandle[KEY_FROM_CITY] = newFromCity
                savedStateHandle[KEY_FROM_CODE] = newFromCode
                savedStateHandle[KEY_TO_CITY]   = newToCity
                savedStateHandle[KEY_TO_CODE]   = newToCode
            }
            is FlightIntent.SearchFlights -> {
                val from = currentState.fromCity
                val to = currentState.toCity
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    setState { copy(isLoading = true, error = null) }
                    when (val result = searchFlightsUseCase(from, to)) {
                        is DataResult.Success -> {
                            setState { copy(flights = result.data.toImmutableList(), isLoading = false) }
                            setEffect(FlightEffect.NavigateToResults(from, to))
                        }
                        is DataResult.Error -> {
                            setState { copy(error = result.exception.message, isLoading = false) }
                            setEffect(FlightEffect.ShowError(result.exception.message ?: "Unknown error"))
                        }
                        // suspend use case — Loading is not a terminal value; isLoading was set before this call.
                        is DataResult.Loading -> Unit
                    }
                }
            }
            is FlightIntent.LoadResults -> {
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    setState { copy(isLoading = true, error = null) }
                    when (val result = searchFlightsUseCase(intent.from, intent.to)) {
                        is DataResult.Success -> setState { copy(flights = result.data.toImmutableList(), isLoading = false) }
                        is DataResult.Error -> {
                            setState { copy(error = result.exception.message, isLoading = false) }
                            setEffect(FlightEffect.ShowError(result.exception.message ?: "Failed to load flights"))
                        }
                        // suspend use case — Loading is not a terminal value; isLoading was set before this call.
                        is DataResult.Loading -> Unit
                    }
                }
            }
        }
    }

    companion object {
        private const val KEY_FROM_CITY      = "from_city"
        private const val KEY_FROM_CODE      = "from_code"
        private const val KEY_TO_CITY        = "to_city"
        private const val KEY_TO_CODE        = "to_code"
        private const val KEY_DEPARTURE_DATE = "departure_date"
        private const val KEY_PASSENGERS     = "passengers"
        private const val KEY_TRIP_TYPE      = "trip_type"
    }
}
