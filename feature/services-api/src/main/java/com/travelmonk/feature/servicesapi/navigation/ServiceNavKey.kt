package com.travelmonk.feature.servicesapi.navigation

import com.travelmonk.core.navigation.TravelNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ServiceNavKey : TravelNavKey {
    @Serializable
    @SerialName("service.root")
    data object Root : ServiceNavKey
}
