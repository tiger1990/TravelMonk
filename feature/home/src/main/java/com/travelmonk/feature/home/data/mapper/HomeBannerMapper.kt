package com.travelmonk.feature.home.data.mapper

import com.travelmonk.feature.home.data.api.dto.HomeBannerDto
import com.travelmonk.feature.home.domain.model.HomeBanner

/**
 * Maps [HomeBannerDto] (network layer) to [HomeBanner] (domain layer).
 * Enhance field mappings here when real backend is integrated.
 */
fun HomeBannerDto.toDomain(): HomeBanner = HomeBanner(
    id = id,
    imageUrl = imageUrl,
    title = title,
    description = description
)
