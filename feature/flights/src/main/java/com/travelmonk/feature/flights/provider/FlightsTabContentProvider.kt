package com.travelmonk.feature.flights.provider

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.feature.flights.mvi.FlightEffect
import com.travelmonk.feature.flights.ui.FlightSearchContent
import com.travelmonk.feature.flights.ui.FlightViewModel
import com.travelmonk.feature.flightsapi.navigation.FlightNavKey
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator
import com.travelmonk.feature.transportapi.TransportTab
import com.travelmonk.feature.transportapi.TransportTabContentProvider
import javax.inject.Inject

class FlightsTabContentProvider @Inject constructor(
    private val flightNavigator: FlightNavigator
) : TransportTabContentProvider {
    override val tab: TransportTab = TransportTab.FLIGHTS

    @Composable
    override fun Content() {
        val viewModel: FlightViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val snackBarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is FlightEffect.NavigateToResults ->
                        flightNavigator.navigateTo(FlightNavKey.Results(effect.from, effect.to))
                    is FlightEffect.ShowError ->
                        snackBarHostState.showSnackbar(effect.message)
                }
            }
        }

        FlightSearchContent(
            state = state,
            onIntent = viewModel::onIntent
        )
    }
}