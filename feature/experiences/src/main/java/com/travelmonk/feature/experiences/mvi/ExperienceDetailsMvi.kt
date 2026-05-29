package com.travelmonk.feature.experiences.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.experiences.domain.model.Experience

@Immutable
data class ExperienceDetailsState(
    val experience: Experience? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface ExperienceDetailsIntent : UiIntent {
    data class LoadDetails(val id: String) : ExperienceDetailsIntent
    data object BookNow : ExperienceDetailsIntent
}

sealed interface ExperienceDetailsEffect : UiEffect {
    data class NavigateToBooking(val experience: Experience) : ExperienceDetailsEffect
    data class ShowError(val message: String) : ExperienceDetailsEffect
}
