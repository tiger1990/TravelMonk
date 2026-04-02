package com.travelmonk.feature.flightsapi.navigator

import com.travelmonk.feature.flightsapi.navigation.FlightNavKey

interface FlightNavigator {
    fun navigateTo(key: FlightNavKey)
    fun back()
}
