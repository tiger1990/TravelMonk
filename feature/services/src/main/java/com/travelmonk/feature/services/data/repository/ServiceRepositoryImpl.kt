package com.travelmonk.feature.services.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.services.data.api.ServicesApi
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
            try {
                DataResult.Success(servicesApi.getServices())
            } catch (e: Exception) {
                DataResult.Error(e)
                /**listOf(
                TravelService("1", "Maid & Helper", "cleaning", "Daily cleaning & domestic help"),
                TravelService("2", "Site Visit", "real_estate", "Property & landmark tours"),
                TravelService("3", "Tour Guide", "person_search", "Expert local storytellers"),
                TravelService("4", "Local Support", "support", "24/7 travel assistance"),
                TravelService("5", "Laundry", "laundry", "Wash & Fold services"),
                TravelService("6", "Car Rental", "car", "Self-drive or chauffeured")
                )
                **/
            }
        }
}
