package com.travelmonk.feature.home.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.home.data.api.HomeApi
import com.travelmonk.feature.home.data.api.dto.HomeBannerDto
import com.travelmonk.feature.home.data.mapper.toDomain
import com.travelmonk.feature.home.domain.model.HomeBanner
import com.travelmonk.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApi: HomeApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HomeRepository {
    override fun getHomeBanners(): Flow<DataResult<List<HomeBanner>>> = flow {
        // TODO: Replace with real API call when backend is integrated:
        // emit(DataResult.Success(homeApi.getHomeBanners().map { it.toDomain() }))
        emit(DataResult.Success(fakeBanners()))
    }.flowOn(ioDispatcher)

    private fun fakeBanners(): List<HomeBanner> = listOf(
        HomeBannerDto("1", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e", "Summer Escape", "Get 20% off on beach resorts").toDomain(),
        HomeBannerDto("2", "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b", "Mountain Trek", "Explore the hidden trails of Alps").toDomain(),
        HomeBannerDto("3", "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1", "City Explorer", "Discover hidden gems in top cities").toDomain()
    )
}
