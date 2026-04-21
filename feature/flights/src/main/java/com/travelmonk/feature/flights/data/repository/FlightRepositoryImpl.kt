package com.travelmonk.feature.flights.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.flights.data.api.FlightsApi
import com.travelmonk.feature.flights.data.api.dto.FlightDto
import com.travelmonk.feature.flights.data.mapper.toDomain
import com.travelmonk.feature.flights.domain.model.Flight
import com.travelmonk.feature.flights.domain.repository.FlightRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FlightRepositoryImpl @Inject constructor(
    private val flightsApi: FlightsApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FlightRepository {
    override suspend fun searchFlights(from: String, to: String): DataResult<List<Flight>> =
        withContext(ioDispatcher) {
            // TODO: Replace with real API call when backend is integrated:
            // DataResult.Success(flightsApi.searchFlights(from, to).map { it.toDomain() })
            DataResult.Success(fakeFlights(from, to))
        }

    private fun fakeFlights(from: String, to: String): List<Flight> = listOf(
        FlightDto("1", "Air Indigo", "08:30", "11:45", "3h 15m", "$120", from, to).toDomain(),
        FlightDto("2", "Sky Jet", "10:15", "13:30", "3h 15m", "$145", from, to).toDomain(),
        FlightDto("3", "Blue Wings", "14:00", "17:20", "3h 20m", "$98", from, to).toDomain()
    )
}
