package com.travelmonk.feature.services.data.api

import com.travelmonk.feature.services.domain.model.TravelService
import retrofit2.http.GET

interface ServicesApi {
    @GET("services")
    suspend fun getServices(): List<TravelService>  // TODO: replace with ServiceDto when real API is integrated
}
