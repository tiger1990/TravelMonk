package com.travelmonk.feature.home.domain.repository

import com.travelmonk.feature.home.domain.model.HomeBanner

interface HomeRepository {
    suspend fun getHomeBanners(): List<HomeBanner>
}
