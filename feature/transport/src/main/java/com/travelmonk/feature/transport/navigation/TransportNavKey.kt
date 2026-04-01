package com.travelmonk.feature.transport.navigation

import com.travelmonk.core.navigation.TravelNavKey

sealed interface TransportNavKey : TravelNavKey {
    data object Root : TransportNavKey
}
