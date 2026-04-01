package com.travelmonk.feature.experiences.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState

data class ExperienceState(
    val selectedCategory: ExperienceCategory = ExperienceCategory.PACKAGES,
    val items: List<ExperienceItem> = emptyList(),
    val isLoading: Boolean = false
) : UiState

enum class ExperienceCategory { PACKAGES, GUIDES, YOGA }

data class ExperienceItem(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val rating: Double,
    val imageUrl: String
)

sealed class ExperienceIntent : UiIntent {
    data class SelectCategory(val category: ExperienceCategory) : ExperienceIntent()
    data class BookItem(val item: ExperienceItem) : ExperienceIntent()
}

sealed class ExperienceEffect : UiEffect {
    data class NavigateToBooking(val item: ExperienceItem) : ExperienceEffect()
}
