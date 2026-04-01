package com.travelmonk.core.model

data class Location(
    val id: String,
    val name: String,
    val code: String? = null,
    val country: String,
    val type: LocationType
)

enum class LocationType {
    CITY, AIRPORT, STATION, HOTEL
}
