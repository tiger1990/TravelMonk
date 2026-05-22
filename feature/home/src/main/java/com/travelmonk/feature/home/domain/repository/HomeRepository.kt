package com.travelmonk.feature.home.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.home.domain.model.HomeBanner
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getHomeBanners(): Flow<DataResult<List<HomeBanner>>>
}
