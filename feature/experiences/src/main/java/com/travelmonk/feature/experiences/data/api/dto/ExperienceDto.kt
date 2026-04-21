package com.travelmonk.feature.experiences.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network DTO for an experience. Mirrors the API response shape.
 * [category] is a raw string from the API; mapped to [com.travelmonk.feature.experiences.domain.model.ExperienceCategory]
 * in ExperienceMapper.
 * Mapped to [com.travelmonk.feature.experiences.domain.model.Experience] via ExperienceMapper.
 */
@Serializable
data class ExperienceDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("price") val price: String,
    @SerialName("rating") val rating: Double,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("category") val category: String
)