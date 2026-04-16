package com.travelmonk.feature.services.domain.repository

import com.travelmonk.feature.services.domain.model.TravelService

interface ServiceRepository {
    suspend fun getServices(): List<TravelService>
}
