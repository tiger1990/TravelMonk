package com.travelmonk.feature.services.data.remote

import com.travelmonk.feature.services.mvi.TravelService
import retrofit2.http.GET

interface ServicesApi {
    @GET("services")
    suspend fun getServices(): List<TravelService>
}
