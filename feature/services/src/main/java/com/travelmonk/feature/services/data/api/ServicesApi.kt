package com.travelmonk.feature.services.data.api

import com.travelmonk.feature.services.data.api.dto.TravelServiceDto
import retrofit2.http.GET

interface ServicesApi {
    @GET("services")
    suspend fun getServices(): List<TravelServiceDto>
}
