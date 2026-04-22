package com.travelmonk.feature.experiences.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.repository.ExperienceRepository
import javax.inject.Inject

class GetExperienceDetailsUseCase @Inject constructor(
    private val repository: ExperienceRepository
) {
    suspend operator fun invoke(id: String): DataResult<Experience> =
        repository.getExperienceById(id)
}
