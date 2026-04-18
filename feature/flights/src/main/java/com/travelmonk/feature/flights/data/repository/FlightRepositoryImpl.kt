package com.travelmonk.feature.flights.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.flights.data.api.FlightsApi
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
            try {
                DataResult.Success(flightsApi.searchFlights(from, to))
            } catch (e: Exception) {
                DataResult.Error(e)
                /**
                 *  // Mock data fallback for demonstration
                 *             listOf(
                 *                 Flight("1", "Air Indigo", "08:30", "11:45", "3h 15m", "$120", from, to),
                 *                 Flight("2", "Sky Jet", "10:15", "13:30", "3h 15m", "$145", from, to)
                 *             )
                 */
            }
        }
}
