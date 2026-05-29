package com.travelmonk.feature.experiences.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.data.api.ExperiencesApi
import com.travelmonk.feature.experiences.data.api.dto.ExperienceDto
import com.travelmonk.feature.experiences.data.mapper.toDomain
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import com.travelmonk.feature.experiences.domain.repository.ExperienceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ExperienceRepositoryImpl @Inject constructor(
    private val experiencesApi: ExperiencesApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ExperienceRepository {

    // Emits Loading immediately so the UI shows a spinner without any explicit setState().
    // The Success emission follows on the IO dispatcher via flowOn().
    //
    // Room migration: replace the flow{} block with:
    //   dao.getExperiences(category).map { DataResult.Success(it.map { e -> e.toDomain() }) }
    override fun getExperiences(category: ExperienceCategory): Flow<DataResult<List<Experience>>> = flow {
        emit(DataResult.Loading)
        // TODO: Replace with real API call when backend is integrated:
        // emit(DataResult.Success(experiencesApi.getExperiences(category.name).map { it.toDomain() }))
        emit(DataResult.Success(fakeExperiences(category)))
    }.flowOn(ioDispatcher)

    override fun getExperienceById(id: String): Flow<DataResult<Experience>> = flow {
        emit(DataResult.Loading)
        // TODO: Replace with real API call when backend is integrated:
        // emit(DataResult.Success(experiencesApi.getExperienceById(id).toDomain()))
        val experience = ExperienceCategory.entries
            .flatMap { fakeExperiences(it) }
            .find { it.id == id }
        if (experience != null) {
            emit(DataResult.Success(experience))
        } else {
            emit(DataResult.Error(Exception("Experience with id $id not found")))
        }
    }.flowOn(ioDispatcher)

    private fun fakeExperiences(category: ExperienceCategory): List<Experience> = when (category) {
        ExperienceCategory.PACKAGES -> listOf(
            ExperienceDto("1", "Bali Paradise", "5 Days, 4 Nights", "$499", 4.8, "https://images.unsplash.com/photo-1537996194471-e657df975ab4", category.name).toDomain(),
            ExperienceDto("2", "Swiss Alps Adventure", "7 Days full tour", "$1200", 4.9, "https://images.unsplash.com/photo-1531310197839-ccf54634509e", category.name).toDomain()
        )
        ExperienceCategory.GUIDES -> listOf(
            ExperienceDto("3", "Local Food Tour - Paris", "With Chef Marco", "$50", 4.7, "https://images.unsplash.com/photo-1550989460-0adf9ea622e2", category.name).toDomain(),
            ExperienceDto("4", "Historical Walk - Rome", "With Dr. Anna", "$40", 4.9, "https://images.unsplash.com/photo-1552832230-c0197dd311b5", category.name).toDomain()
        )
        ExperienceCategory.YOGA -> listOf(
            ExperienceDto("5", "Sunrise Yoga Session", "Beachfront - 2h", "$25", 4.8, "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b", category.name).toDomain(),
            ExperienceDto("6", "Zen Meditation Retreat", "Weekend Package", "$150", 5.0, "https://images.unsplash.com/photo-1506126613408-eca07ce68773", category.name).toDomain()
        )
    }
}
