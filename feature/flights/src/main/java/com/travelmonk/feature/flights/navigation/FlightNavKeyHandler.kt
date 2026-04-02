package com.travelmonk.feature.flights.navigation

import com.travelmonk.core.navigation.NavDestination
import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.flightsapi.navigation.FlightNavKey
import javax.inject.Inject

// Flights are a sub-flow of the Transport tab
class FlightNavKeyHandler @Inject constructor() : NavKeyHandler {
    override fun canHandle(key: TravelNavKey) = key is FlightNavKey
    override fun resolve(key: TravelNavKey) = NavDestination(key, NavTab.TRANSPORT)
}
