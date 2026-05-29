package com.travelmonk.feature.stays.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState

@Immutable
data class StaySearchState(
    val location: String = "Paris, France",
    val stayType: StayType = StayType.HOTEL,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

enum class StayType { HOTEL, APARTMENT, RESORT }

sealed interface StayIntent : UiIntent {
    data class ChangeStayType(val type: StayType) : StayIntent
    data class UpdateLocation(val location: String) : StayIntent
    data object SearchStays : StayIntent
}

sealed interface StayEffect : UiEffect {
    data class NavigateToResults(val location: String) : StayEffect
    data class ShowError(val message: String) : StayEffect
}
