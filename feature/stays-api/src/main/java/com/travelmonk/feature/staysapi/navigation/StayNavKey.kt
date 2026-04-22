package com.travelmonk.feature.staysapi.navigation

import com.travelmonk.core.navigation.TravelNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface StayNavKey : TravelNavKey {
    @Serializable
    @SerialName("stay.search")
    data object Search : StayNavKey

    @Serializable
    @SerialName("stay.results")
    data class Results(val location: String) : StayNavKey

    @Serializable
    @SerialName("stay.details")
    data class Details(val stayId: String) : StayNavKey
}
