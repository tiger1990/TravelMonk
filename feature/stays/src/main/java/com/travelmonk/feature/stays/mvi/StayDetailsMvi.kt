package com.travelmonk.feature.stays.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.stays.domain.model.Stay

data class StayDetailsState(
    val stay: Stay? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed class StayDetailsIntent : UiIntent {
    data class LoadDetails(val stayId: String) : StayDetailsIntent()
    data object BookNow : StayDetailsIntent()
    data object ToggleFavorite : StayDetailsIntent()
}

sealed class StayDetailsEffect : UiEffect {
    data class NavigateToBooking(val stay: Stay) : StayDetailsEffect()
    data class ShowError(val message: String) : StayDetailsEffect()
}
