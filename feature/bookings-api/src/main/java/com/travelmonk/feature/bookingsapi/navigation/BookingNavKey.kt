package com.travelmonk.feature.bookingsapi.navigation

import com.travelmonk.core.model.BookingType
import com.travelmonk.core.navigation.TravelNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface BookingNavKey : TravelNavKey {
    @Serializable
    @SerialName("booking.root")
    data object Root : BookingNavKey

    @Serializable
    @SerialName("booking.confirmation")
    data class Confirmation(val type: BookingType, val title: String) : BookingNavKey
}
