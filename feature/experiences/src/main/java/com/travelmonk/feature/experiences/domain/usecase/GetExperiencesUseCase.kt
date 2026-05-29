package com.travelmonk.feature.experiences.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import com.travelmonk.feature.experiences.domain.repository.ExperienceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExperiencesUseCase @Inject constructor(
    private val repository: ExperienceRepository
) {
    operator fun invoke(category: ExperienceCategory): Flow<DataResult<List<Experience>>> =
        repository.getExperiences(category)
}
