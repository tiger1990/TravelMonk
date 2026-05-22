package com.travelmonk.feature.services.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.services.domain.model.TravelService
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    fun getServices(): Flow<DataResult<List<TravelService>>>
}
