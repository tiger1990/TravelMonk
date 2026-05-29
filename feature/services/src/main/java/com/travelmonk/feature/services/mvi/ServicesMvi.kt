package com.travelmonk.feature.services.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.services.domain.model.TravelService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ServicesState(
    val isLoading: Boolean = false,
    val services: ImmutableList<TravelService> = persistentListOf(),
    val selectedService: TravelService? = null
) : UiState

sealed interface ServicesIntent : UiIntent {
    data class SelectService(val service: TravelService) : ServicesIntent
}

sealed interface ServicesEffect : UiEffect {
    data class NavigateToBooking(val service: TravelService) : ServicesEffect
}
