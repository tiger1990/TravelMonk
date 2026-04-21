package com.travelmonk.feature.flights.data.api

import com.travelmonk.feature.flights.data.api.dto.FlightDto
import retrofit2.http.GET
import retrofit2.http.Query

interface FlightsApi {
    @GET("flights/search")
    suspend fun searchFlights(
        @Query("from") from: String,
        @Query("to") to: String
    ): List<FlightDto>
}
