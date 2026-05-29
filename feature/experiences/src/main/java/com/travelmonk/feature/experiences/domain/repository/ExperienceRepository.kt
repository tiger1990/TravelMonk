package com.travelmonk.feature.experiences.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import kotlinx.coroutines.flow.Flow

interface ExperienceRepository {
    // Returns a cold Flow so callers get reactive updates (Room migration: swap
    // flow{emit(fake)} for dao.getExperiences(category).map { DataResult.Success(it) }).
    fun getExperiences(category: ExperienceCategory): Flow<DataResult<List<Experience>>>

    // Returns a cold Flow (mirrors getExperiences pattern; Room migration: swap
    // flow{} for dao.getExperienceById(id).map { DataResult.Success(it.toDomain()) }).
    fun getExperienceById(id: String): Flow<DataResult<Experience>>
}
