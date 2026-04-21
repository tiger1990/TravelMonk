package com.travelmonk.feature.home.data.api

import com.travelmonk.feature.home.data.api.dto.HomeBannerDto
import retrofit2.http.GET

interface HomeApi {
    @GET("home/banners")
    suspend fun getHomeBanners(): List<HomeBannerDto>
}
