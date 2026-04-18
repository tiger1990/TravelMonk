package com.travelmonk.feature.flights.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.flights.domain.model.Flight
import com.travelmonk.feature.flights.domain.repository.FlightRepository
import javax.inject.Inject

class SearchFlightsUseCase @Inject constructor(
    private val repository: FlightRepository
) {
    suspend operator fun invoke(from: String, to: String): DataResult<List<Flight>> =
        repository.searchFlights(from, to)
}
