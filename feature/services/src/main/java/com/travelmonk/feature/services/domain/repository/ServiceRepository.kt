package com.travelmonk.feature.services.domain.repository

import com.travelmonk.feature.services.mvi.TravelService

interface ServiceRepository {
    suspend fun getServices(): List<TravelService>
}
