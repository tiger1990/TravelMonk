package com.travelmonk.feature.bookings.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network DTO for a booking. Mirrors the API response shape.
 * [type] and [status] are raw strings from the API; mapped to domain enums in BookingMapper.
 * Mapped to [com.travelmonk.core.model.Booking] via BookingMapper.
 */
@Serializable
data class BookingDto(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("title") val title: String,
    @SerialName("date") val date: String,
    @SerialName("status") val status: String,
    @SerialName("price") val price: String
)
