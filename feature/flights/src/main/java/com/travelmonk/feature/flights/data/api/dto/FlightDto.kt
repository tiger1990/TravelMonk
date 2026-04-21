package com.travelmonk.feature.flights.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network DTO for a flight result. Mirrors the API response shape.
 * Mapped to [com.travelmonk.feature.flights.domain.model.Flight] via FlightMapper.
 */
@Serializable
data class FlightDto(
    @SerialName("id") val id: String,
    @SerialName("airline") val airline: String,
    @SerialName("departure_time") val departureTime: String,
    @SerialName("arrival_time") val arrivalTime: String,
    @SerialName("duration") val duration: String,
    @SerialName("price") val price: String,
    @SerialName("from_code") val fromCode: String,
    @SerialName("to_code") val toCode: String
)