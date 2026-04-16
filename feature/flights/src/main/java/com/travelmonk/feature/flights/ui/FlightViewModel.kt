package com.travelmonk.feature.flights.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.feature.flights.domain.usecase.SearchFlightsUseCase
import com.travelmonk.feature.flights.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val searchFlightsUseCase: SearchFlightsUseCase
) : BaseViewModel<FlightSearchState, FlightIntent, FlightEffect>() {
    override fun createInitialState(): FlightSearchState = FlightSearchState()

    override fun handleIntent(intent: FlightIntent) {
        when (intent) {
            is FlightIntent.ChangeTripType -> setState { copy(tripType = intent.type) }
            is FlightIntent.SwapCities -> setState {
                copy(fromCity = intent.to, fromCode = toCode, toCity = intent.from, toCode = fromCode)
            }
            is FlightIntent.SearchFlights -> {
                val from = currentState.fromCity
                val to = currentState.toCity
                viewModelScope.launch {
                    setState { copy(isLoading = true) }
                    try {
                        searchFlightsUseCase(from, to)
                        setEffect(FlightEffect.NavigateToResults(from, to))
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
