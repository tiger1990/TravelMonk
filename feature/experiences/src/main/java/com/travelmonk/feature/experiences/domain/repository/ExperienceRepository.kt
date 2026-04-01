package com.travelmonk.feature.experiences.domain.repository

import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory

interface ExperienceRepository {
    suspend fun getExperiences(category: ExperienceCategory): List<Experience>
}
