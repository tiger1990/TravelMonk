package com.travelmonk.feature.services.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.services.domain.model.TravelService
import com.travelmonk.feature.services.domain.repository.ServiceRepository
import javax.inject.Inject

class GetServicesUseCase @Inject constructor(
    private val repository: ServiceRepository
) {
    suspend operator fun invoke(): DataResult<List<TravelService>> =
        repository.getServices()
}
