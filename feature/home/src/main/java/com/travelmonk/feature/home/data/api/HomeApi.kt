package com.travelmonk.feature.home.data.api

import com.travelmonk.feature.home.domain.model.HomeBanner
import retrofit2.http.GET

interface HomeApi {
    @GET("home/banners")
    suspend fun getHomeBanners(): List<HomeBanner>  // TODO: replace with HomeBannerDto when real API is integrated
}
