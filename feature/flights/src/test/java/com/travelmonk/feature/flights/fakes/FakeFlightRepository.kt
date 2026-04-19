package com.travelmonk.feature.flights.fakes

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.flights.domain.model.Flight
import com.travelmonk.feature.flights.domain.repository.FlightRepository

class FakeFlightRepository : FlightRepository {
    var result: DataResult<List<Flight>> = DataResult.Success(emptyList())

    override suspend fun searchFlights(from: String, to: String): DataResult<List<Flight>> = result
}