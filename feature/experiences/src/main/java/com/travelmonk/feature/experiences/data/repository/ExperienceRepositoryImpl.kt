package com.travelmonk.feature.experiences.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.data.api.ExperiencesApi
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import com.travelmonk.feature.experiences.domain.repository.ExperienceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExperienceRepositoryImpl @Inject constructor(
    private val experiencesApi: ExperiencesApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ExperienceRepository {
    override suspend fun getExperiences(category: ExperienceCategory): DataResult<List<Experience>> =
        withContext(ioDispatcher) {
            try {
                DataResult.Success(experiencesApi.getExperiences(category.name))
            } catch (e: Exception) {
                DataResult.Error(e)
                /**
                 * ExperienceCategory.PACKAGES -> listOf(
                 *                         Experience("1", "Bali Paradise", "5 Days, 4 Nights", "$499", 4.8, "https://images.unsplash.com/photo-1537996194471-e657df975ab4", category),
                 *                         Experience("2", "Swiss Alps Adventure", "7 Days full tour", "$1200", 4.9, "https://images.unsplash.com/photo-1531310197839-ccf54634509e", category)
                 *                     )
                 *                     ExperienceCategory.GUIDES -> listOf(
                 *                         Experience("3", "Local Food Tour - Paris", "With Chef Marco", "$50", 4.7, "https://images.unsplash.com/photo-1550989460-0adf9ea622e2", category),
                 *                         Experience("4", "Historical Walk - Rome", "With Dr. Anna", "$40", 4.9, "https://images.unsplash.com/photo-1552832230-c0197dd311b5", category)
                 *                     )
                 *                     ExperienceCategory.YOGA -> listOf(
                 *                         Experience("5", "Sunrise Yoga Session", "Beachfront - 2h", "$25", 4.8, "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b", category),
                 *                         Experience("6", "Zen Meditation Retreat", "Weekend Package", "$150", 5.0, "https://images.unsplash.com/photo-1506126613408-eca07ce68773", category)
                 *                     )
                 */
            }
        }
}
