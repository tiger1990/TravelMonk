package com.travelmonk.feature.transportapi.navigator

import androidx.compose.runtime.Stable

@Stable // All implementations are Hilt singletons; safe for Compose to skip recomposition
interface TransportNavigator {
    fun back()
    // future: navigateToFlightSearch(), navigateToBusSearch(), navigateToTrainSearch()
}
