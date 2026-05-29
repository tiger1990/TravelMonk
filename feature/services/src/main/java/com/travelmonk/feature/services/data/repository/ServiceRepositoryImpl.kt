package com.travelmonk.feature.services.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.services.data.api.ServicesApi
import com.travelmonk.feature.services.data.api.dto.TravelServiceDto
import com.travelmonk.feature.services.data.mapper.toDomain
import com.travelmonk.feature.services.domain.model.TravelService
import com.travelmonk.feature.services.domain.repository.ServiceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ServiceRepositoryImpl @Inject constructor(
    private val servicesApi: ServicesApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ServiceRepository {
    override fun getServices(): Flow<DataResult<List<TravelService>>> = flow {
        // TODO: Replace with real API call when backend is integrated:
        // emit(DataResult.Success(servicesApi.getServices().map { it.toDomain() }))
        emit(DataResult.Success(fakeServices()))
    }.flowOn(ioDispatcher)

    private fun fakeServices(): List<TravelService> = listOf(
        TravelServiceDto("1", "Maid & Helper", "cleaning_services",     "Daily cleaning & domestic help").toDomain(),
        TravelServiceDto("2", "Site Visit",    "real_estate_agent",     "Property & landmark tours").toDomain(),
        TravelServiceDto("3", "Tour Guide",    "person_search",         "Expert local storytellers").toDomain(),
        TravelServiceDto("4", "Local Support", "support_agent",         "24/7 travel assistance").toDomain(),
        TravelServiceDto("5", "Laundry",       "local_laundry_service", "Wash & Fold services").toDomain(),
        TravelServiceDto("6", "Car Rental",    "directions_car",        "Self-drive or chauffeured").toDomain()
    )
}
