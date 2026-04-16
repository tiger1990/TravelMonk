package com.travelmonk.feature.experiences.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory

data class ExperienceState(
    val selectedCategory: ExperienceCategory = ExperienceCategory.PACKAGES,
    val items: List<Experience> = emptyList(),
    val isLoading: Boolean = false
) : UiState

sealed class ExperienceIntent : UiIntent {
    data class SelectCategory(val category: ExperienceCategory) : ExperienceIntent()
    data class BookItem(val item: Experience) : ExperienceIntent()
}

sealed class ExperienceEffect : UiEffect {
    data class NavigateToBooking(val item: Experience) : ExperienceEffect()
}
