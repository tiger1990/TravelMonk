package com.travelmonk.feature.services.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.services.data.api.ServicesApi
import com.travelmonk.feature.services.data.api.dto.TravelServiceDto
import com.travelmonk.feature.services.data.mapper.toDomain
import com.travelmonk.feature.services.domain.model.TravelService
import com.travelmonk.feature.services.domain.repository.ServiceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ServiceRepositoryImpl @Inject constructor(
    private val servicesApi: ServicesApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ServiceRepository {
    override suspend fun getServices(): DataResult<List<TravelService>> =
        withContext(ioDispatcher) {
            // TODO: Replace with real API call when backend is integrated:
            // DataResult.Success(servicesApi.getServices().map { it.toDomain() })
            DataResult.Success(fakeServices())
        }

    private fun fakeServices(): List<TravelService> = listOf(
        TravelServiceDto("1", "Maid & Helper", "cleaning", "Daily cleaning & domestic help").toDomain(),
        TravelServiceDto("2", "Site Visit", "real_estate", "Property & landmark tours").toDomain(),
        TravelServiceDto("3", "Tour Guide", "person_search", "Expert local storytellers").toDomain(),
        TravelServiceDto("4", "Local Support", "support", "24/7 travel assistance").toDomain(),
        TravelServiceDto("5", "Laundry", "laundry", "Wash & Fold services").toDomain(),
        TravelServiceDto("6", "Car Rental", "car", "Self-drive or chauffeured").toDomain()
    )
}
