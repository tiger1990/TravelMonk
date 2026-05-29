package com.travelmonk.feature.experiences.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ExperienceState(
    val selectedCategory: ExperienceCategory = ExperienceCategory.PACKAGES,
    val items: ImmutableList<Experience> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface ExperienceIntent : UiIntent {
    data class SelectCategory(val category: ExperienceCategory) : ExperienceIntent
    data class SelectExperience(val experienceId: String) : ExperienceIntent
    data class BookItem(val item: Experience) : ExperienceIntent
}

sealed interface ExperienceEffect : UiEffect {
    data class NavigateToDetail(val experienceId: String) : ExperienceEffect
    data class NavigateToBooking(val item: Experience) : ExperienceEffect
    data class ShowError(val message: String) : ExperienceEffect
}
