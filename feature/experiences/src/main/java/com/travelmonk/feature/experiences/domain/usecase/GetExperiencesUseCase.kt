package com.travelmonk.feature.experiences.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import com.travelmonk.feature.experiences.domain.repository.ExperienceRepository
import javax.inject.Inject

class GetExperiencesUseCase @Inject constructor(
    private val repository: ExperienceRepository
) {
    suspend operator fun invoke(category: ExperienceCategory): DataResult<List<Experience>> =
        repository.getExperiences(category)
}
