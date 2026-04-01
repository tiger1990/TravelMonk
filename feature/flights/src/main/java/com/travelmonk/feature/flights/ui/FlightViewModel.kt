package com.travelmonk.feature.flights.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.feature.flights.domain.repository.FlightRepository
import com.travelmonk.feature.flights.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val flightRepository: FlightRepository
) : BaseViewModel<FlightSearchState, FlightIntent, FlightEffect>() {
    override fun createInitialState(): FlightSearchState = FlightSearchState()

    override fun handleIntent(intent: FlightIntent) {
        when (intent) {
            is FlightIntent.ChangeTripType -> setState { copy(tripType = intent.type) }
            is FlightIntent.SwapCities -> setState { 
                copy(fromCity = intent.to, fromCode = currentState.toCode, toCity = intent.from, toCode = currentState.fromCode) 
            }
            is FlightIntent.SearchFlights -> {
                viewModelScope.launch {
                    setState { copy(isLoading = true) }
                    try {
                        val results = flightRepository.searchFlights(currentState.fromCity, currentState.toCity)
                        // In a real flow, we might store results in state or pass them via effect
                        setEffect(FlightEffect.NavigateToResults(currentState.fromCity, currentState.toCity))
                    } catch (e: Exception) {
                        setEffect(FlightEffect.ShowError(e.message ?: "Unknown error"))
                    } finally {
                        setState { copy(isLoading = false) }
                    }
                }
            }
        }
    }
}
