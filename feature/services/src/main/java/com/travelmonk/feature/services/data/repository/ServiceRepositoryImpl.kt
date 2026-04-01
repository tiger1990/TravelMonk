package com.travelmonk.feature.services.data.repository

import com.travelmonk.feature.services.data.remote.ServicesApi
import com.travelmonk.feature.services.domain.repository.ServiceRepository
import com.travelmonk.feature.services.mvi.TravelService
import javax.inject.Inject

class ServiceRepositoryImpl @Inject constructor(
    private val servicesApi: ServicesApi
) : ServiceRepository {
    override suspend fun getServices(): List<TravelService> {
        return try {
            servicesApi.getServices()
        } catch (e: Exception) {
            listOf(
                TravelService("1", "Maid & Helper", "cleaning", "Daily cleaning & domestic help"),
                TravelService("2", "Site Visit", "real_estate", "Property & landmark tours"),
                TravelService("3", "Tour Guide", "person_search", "Expert local storytellers"),
                TravelService("4", "Local Support", "support", "24/7 travel assistance"),
                TravelService("5", "Laundry", "laundry", "Wash & Fold services"),
                TravelService("6", "Car Rental", "car", "Self-drive or chauffeured")
            )
        }
    }
}
