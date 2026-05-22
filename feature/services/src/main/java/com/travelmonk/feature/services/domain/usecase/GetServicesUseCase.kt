package com.travelmonk.feature.services.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.services.domain.model.TravelService
import com.travelmonk.feature.services.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetServicesUseCase @Inject constructor(
    private val repository: ServiceRepository
) {
    operator fun invoke(): Flow<DataResult<List<TravelService>>> = repository.getServices()
}
