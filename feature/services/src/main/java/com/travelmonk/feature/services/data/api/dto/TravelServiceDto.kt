package com.travelmonk.feature.services.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network DTO for a travel service. Mirrors the API response shape.
 * Mapped to [com.travelmonk.feature.services.domain.model.TravelService] via TravelServiceMapper.
 */
@Serializable
data class TravelServiceDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("icon_name") val iconName: String,
    @SerialName("description") val description: String
)
