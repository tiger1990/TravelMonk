package com.travelmonk.feature.flights.data.repository

import com.travelmonk.feature.flights.data.remote.FlightsApi
import com.travelmonk.feature.flights.domain.model.Flight
import com.travelmonk.feature.flights.domain.repository.FlightRepository
import javax.inject.Inject

class FlightRepositoryImpl @Inject constructor(
    private val flightsApi: FlightsApi
) : FlightRepository {
    override suspend fun searchFlights(from: String, to: String): List<Flight> {
        // In a real app, we would call the API. 
        // For now, returning mock data or calling the API if implemented.
        return try {
            flightsApi.searchFlights(from, to)
        } catch (e: Exception) {
            // Mock data fallback for demonstration
            listOf(
                Flight("1", "Air Indigo", "08:30", "11:45", "3h 15m", "$120", from, to),
                Flight("2", "Sky Jet", "10:15", "13:30", "3h 15m", "$145", from, to)
            )
        }
    }
}
