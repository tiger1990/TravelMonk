package com.travelmonk.feature.flights.fixtures

import com.travelmonk.feature.flights.domain.model.Flight

object FlightFixtures {
    val sampleFlight = Flight(
        id = "FL001",
        airline = "Air Indigo",
        departureTime = "08:30",
        arrivalTime = "11:45",
        duration = "3h 15m",
        price = "$120",
        fromCode = "SFO",
        toCode = "JFK"
    )

    val sampleFlights = listOf(
        sampleFlight,
        sampleFlight.copy(id = "FL002", airline = "Sky Jet", price = "$145"),
        sampleFlight.copy(id = "FL003", airline = "Star Airways", price = "$110")
    )
}
