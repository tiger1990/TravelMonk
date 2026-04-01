package com.travelmonk.feature.home.data.remote

import com.travelmonk.feature.home.domain.model.HomeBanner
import retrofit2.http.GET

interface HomeApi {
    @GET("home/banners")
    suspend fun getHomeBanners(): List<HomeBanner>
}
