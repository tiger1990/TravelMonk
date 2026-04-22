package com.travelmonk.feature.stays.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.stays.domain.model.Stay

data class StayResultsState(
    val location: String = "",
    val stays: List<Stay> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed class StayResultsIntent : UiIntent {
    data class LoadStays(val location: String) : StayResultsIntent()
    data class ToggleFavorite(val stayId: String) : StayResultsIntent()
    data class SelectStay(val stay: Stay) : StayResultsIntent()
}

sealed class StayResultsEffect : UiEffect {
    data class NavigateToDetail(val stayId: String) : StayResultsEffect()
    data class ShowError(val message: String) : StayResultsEffect()
}
