package com.travelmonk.feature.flights.navigator

import com.travelmonk.feature.flights.navigation.FlightNavKey

interface FlightNavigator {
    fun navigateTo(key: FlightNavKey)
    fun back()
}
