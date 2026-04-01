package com.travelmonk.feature.stays.data.remote

import com.travelmonk.feature.stays.domain.model.Stay
import retrofit2.http.GET
import retrofit2.http.Query

interface StaysApi {
    @GET("stays/search")
    suspend fun searchStays(
        @Query("location") location: String
    ): List<Stay>
}
