package com.travelmonk.feature.flights.data.remote

import com.travelmonk.feature.flights.domain.model.Flight
import retrofit2.http.GET
import retrofit2.http.Query

interface FlightsApi {
    @GET("flights/search")
    suspend fun searchFlights(
        @Query("from") from: String,
        @Query("to") to: String
    ): List<Flight>
}
