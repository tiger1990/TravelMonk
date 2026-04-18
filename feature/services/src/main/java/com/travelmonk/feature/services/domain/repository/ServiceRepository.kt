package com.travelmonk.feature.services.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.services.domain.model.TravelService

interface ServiceRepository {
    suspend fun getServices(): DataResult<List<TravelService>>
}
