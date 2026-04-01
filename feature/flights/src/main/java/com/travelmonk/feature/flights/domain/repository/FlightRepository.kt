package com.travelmonk.feature.flights.domain.repository

import com.travelmonk.feature.flights.domain.model.Flight

interface FlightRepository {
    suspend fun searchFlights(from: String, to: String): List<Flight>
}
