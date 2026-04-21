package com.travelmonk.feature.stays.data.mapper

import com.travelmonk.feature.stays.data.api.dto.StayDto
import com.travelmonk.feature.stays.domain.model.Stay

/**
 * Maps [StayDto] (network layer) to [Stay] (domain layer).
 * Enhance field mappings here when real backend is integrated.
 */
fun StayDto.toDomain(): Stay = Stay(
    id = id,
    title = title,
    location = location,
    price = price,
    rating = rating,
    imageUrl = imageUrl
)
