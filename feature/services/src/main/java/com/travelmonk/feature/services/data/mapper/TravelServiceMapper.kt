package com.travelmonk.feature.services.data.mapper

import com.travelmonk.feature.services.data.api.dto.TravelServiceDto
import com.travelmonk.feature.services.domain.model.TravelService

/**
 * Maps [TravelServiceDto] (network layer) to [TravelService] (domain layer).
 * Enhance field mappings here when real backend is integrated.
 */
fun TravelServiceDto.toDomain(): TravelService = TravelService(
    id = id,
    name = name,
    iconName = iconName,
    description = description
)
