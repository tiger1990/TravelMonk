package com.travelmonk.feature.stays.data.repository

import com.travelmonk.feature.stays.data.remote.StaysApi
import com.travelmonk.feature.stays.domain.model.Stay
import com.travelmonk.feature.stays.domain.repository.StayRepository
import javax.inject.Inject

class StayRepositoryImpl @Inject constructor(
    private val staysApi: StaysApi
) : StayRepository {
    override suspend fun searchStays(location: String): List<Stay> {
        return try {
            staysApi.searchStays(location)
        } catch (e: Exception) {
            listOf(
                Stay("1", "The Grand Oberoi", location, "$240", "4.9", "https://images.unsplash.com/photo-1566073771259-6a8506099945"),
                Stay("2", "Azure Apartment", location, "$180", "4.7", "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267")
            )
        }
    }
}
