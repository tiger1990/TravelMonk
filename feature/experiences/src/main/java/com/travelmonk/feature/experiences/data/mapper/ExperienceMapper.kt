package com.travelmonk.feature.experiences.data.mapper

import com.travelmonk.feature.experiences.data.api.dto.ExperienceDto
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory

/**
 * Maps [ExperienceDto] (network layer) to [Experience] (domain layer).
 * [category] is a raw API string; unknown values fall back to [ExperienceCategory.PACKAGES].
 * Enhance field mappings here when real backend is integrated.
 */
fun ExperienceDto.toDomain(): Experience = Experience(
    id = id,
    title = title,
    description = description,
    price = price,
    rating = rating,
    imageUrl = imageUrl,
    category = ExperienceCategory.entries.firstOrNull { it.name == category }
        ?: ExperienceCategory.PACKAGES
)
