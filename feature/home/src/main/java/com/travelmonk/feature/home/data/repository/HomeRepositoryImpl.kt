package com.travelmonk.feature.home.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.feature.home.data.api.HomeApi
import com.travelmonk.feature.home.domain.model.HomeBanner
import com.travelmonk.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApi: HomeApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HomeRepository {
    override suspend fun getHomeBanners(): List<HomeBanner> =
        withContext(ioDispatcher) {
            try {
                homeApi.getHomeBanners()
            } catch (e: Exception) {
                listOf(
                    HomeBanner("1", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e", "Summer Escape", "Get 20% off on beach resorts"),
                    HomeBanner("2", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b", "Mountain Trek", "Explore the hidden trails of Alps")
                )
            }
        }
}
