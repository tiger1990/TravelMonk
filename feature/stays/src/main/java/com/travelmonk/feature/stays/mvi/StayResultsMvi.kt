package com.travelmonk.feature.stays.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.stays.domain.model.Stay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class StayResultsState(
    val location: String = "",
    val stays: ImmutableList<Stay> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface StayResultsIntent : UiIntent {
    // Retired: location now arrives via @AssistedInject; initial load is seeded in the ViewModel's init{}.
    // data class LoadStays(val location: String) : StayResultsIntent
    data object Retry : StayResultsIntent
    data class ToggleFavorite(val stayId: String) : StayResultsIntent
    data class SelectStay(val stay: Stay) : StayResultsIntent
}

sealed interface StayResultsEffect : UiEffect {
    data class NavigateToDetail(val stayId: String) : StayResultsEffect
    data class ShowError(val message: String) : StayResultsEffect
}
