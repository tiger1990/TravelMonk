package com.travelmonk.feature.flights.provider

import androidx.compose.runtime.Composable
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator
import com.travelmonk.feature.flights.ui.FlightSearchScreen
import com.travelmonk.feature.transportapi.TransportTab
import com.travelmonk.feature.transportapi.TransportTabContentProvider
import javax.inject.Inject

class FlightsTabContentProvider @Inject constructor(
    private val flightNavigator: FlightNavigator
) : TransportTabContentProvider {
    override val tab: TransportTab = TransportTab.FLIGHTS
    @Composable
    override fun Content() {
        FlightSearchScreen(navigator = flightNavigator)
    }
}