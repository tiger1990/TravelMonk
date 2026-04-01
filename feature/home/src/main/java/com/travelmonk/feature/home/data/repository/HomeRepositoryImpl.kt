package com.travelmonk.feature.home.data.repository

import com.travelmonk.feature.home.data.remote.HomeApi
import com.travelmonk.feature.home.domain.model.HomeBanner
import com.travelmonk.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApi: HomeApi
) : HomeRepository {
    override suspend fun getHomeBanners(): List<HomeBanner> {
        return try {
            homeApi.getHomeBanners()
        } catch (e: Exception) {
            // Mock data fallback
            listOf(
                HomeBanner("1", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e", "Summer Escape", "Get 20% off on beach resorts"),
                HomeBanner("2", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b", "Mountain Trek", "Explore the hidden trails of Alps")
            )
        }
    }
}
