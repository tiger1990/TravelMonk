package com.travelmonk.feature.home.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.home.domain.model.HomeBanner

interface HomeRepository {
    suspend fun getHomeBanners(): DataResult<List<HomeBanner>>
}
