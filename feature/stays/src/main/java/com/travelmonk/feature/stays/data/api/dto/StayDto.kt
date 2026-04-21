package com.travelmonk.feature.stays.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network DTO for a stay/hotel result. Mirrors the API response shape.
 * Mapped to [com.travelmonk.feature.stays.domain.model.Stay] via StayMapper.
 */
@Serializable
data class StayDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("location") val location: String,
    @SerialName("price") val price: String,
    @SerialName("rating") val rating: String,
    @SerialName("image_url") val imageUrl: String
)