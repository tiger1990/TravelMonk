package com.travelmonk.feature.flights.domain.model

data class Flight(
    val id: String,
    val airline: String,
    val departureTime: String,
    val arrivalTime: String,
    val duration: String,
    val price: String,
    val fromCode: String,
    val toCode: String
)
