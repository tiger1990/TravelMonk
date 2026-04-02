package com.travelmonk.feature.transportapi.navigation

import com.travelmonk.core.navigation.TravelNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface TransportNavKey : TravelNavKey {
    @Serializable
    @SerialName("transport.root")
    data object Root : TransportNavKey
}
