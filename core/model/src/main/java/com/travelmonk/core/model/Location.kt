package com.travelmonk.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("code") val code: String? = null,
    @SerialName("country") val country: String,
    @SerialName("type") val type: LocationType
)

@Serializable
enum class LocationType {
    @SerialName("city") CITY,
    @SerialName("airport") AIRPORT,
    @SerialName("station") STATION,
    @SerialName("hotel") HOTEL
}
