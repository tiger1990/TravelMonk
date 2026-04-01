package com.travelmonk.core.model

data class Booking(
    val id: String,
    val type: BookingType,
    val title: String,
    val date: String,
    val status: BookingStatus,
    val price: String
)

enum class BookingType {
    FLIGHT, BUS, TRAIN, HOTEL, PACKAGE, SERVICE
}

enum class BookingStatus {
    CONFIRMED, UPCOMING, PENDING, CANCELLED
}
