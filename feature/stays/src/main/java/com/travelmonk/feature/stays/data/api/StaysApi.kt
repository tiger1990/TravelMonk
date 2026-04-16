package com.travelmonk.feature.stays.data.api

import com.travelmonk.feature.stays.domain.model.Stay
import retrofit2.http.GET
import retrofit2.http.Query

interface StaysApi {
    @GET("stays/search")
    suspend fun searchStays(
        @Query("location") location: String
    ): List<Stay>  // TODO: replace with StayDto when real API is integrated
}
