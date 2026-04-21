package com.travelmonk.feature.home.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network DTO for a home banner. Mirrors the API response shape.
 * Mapped to [com.travelmonk.feature.home.domain.model.HomeBanner] via HomeBannerMapper.
 */
@Serializable
data class HomeBannerDto(
    @SerialName("id") val id: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String
)
