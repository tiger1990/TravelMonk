package com.travelmonk.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    @SerialName("id") val id: String,
    @SerialName("type") val type: BookingType,
    @SerialName("title") val title: String,
    @SerialName("date") val date: String,
    @SerialName("status") val status: BookingStatus,
    @SerialName("price") val price: String
)

@Serializable
enum class BookingType {
    @SerialName("flight") FLIGHT,

    @SerialName("bus") BUS,

    @SerialName("train") TRAIN,
    @SerialName("hotel") HOTEL,
    @SerialName("package") PACKAGE,
    @SerialName("service") SERVICE,
}

@Serializable
enum class BookingStatus {
    @SerialName("confirmed") CONFIRMED,
    @SerialName("upcoming") UPCOMING,
    @SerialName("pending") PENDING,
    @SerialName("cancelled") CANCELLED
}
