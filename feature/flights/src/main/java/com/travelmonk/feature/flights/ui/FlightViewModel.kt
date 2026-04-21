package com.travelmonk.feature.flights.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
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
                    setState { copy(isLoading = true, error = null) }
                    when (val result = searchFlightsUseCase(from, to)) {
                        is DataResult.Success -> {
                            setState { copy(flights = result.data, isLoading = false) }
                            setEffect(FlightEffect.NavigateToResults(from, to))
                        }
                        is DataResult.Error -> {
                            setState { copy(error = result.exception.message, isLoading = false) }
                            setEffect(FlightEffect.ShowError(result.exception.message ?: "Unknown error"))
                        }
                        is DataResult.Loading -> Unit
                    }
                }
            }
            is FlightIntent.LoadResults -> {
                viewModelScope.launch {
                    setState { copy(isLoading = true, error = null) }
                    when (val result = searchFlightsUseCase(intent.from, intent.to)) {
                        is DataResult.Success -> setState { copy(flights = result.data, isLoading = false) }
                        is DataResult.Error -> {
                            setState { copy(error = result.exception.message, isLoading = false) }
                            setEffect(FlightEffect.ShowError(result.exception.message ?: "Failed to load flights"))
                        }
                        is DataResult.Loading -> Unit
                    }
                }
            }
        }
    }
}
