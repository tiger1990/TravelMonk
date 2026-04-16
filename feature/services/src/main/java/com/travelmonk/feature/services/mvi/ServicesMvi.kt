package com.travelmonk.feature.services.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.services.domain.model.TravelService

data class ServicesState(
    val isLoading: Boolean = false,
    val services: List<TravelService> = emptyList(),
    val selectedService: TravelService? = null
) : UiState

sealed class ServicesIntent : UiIntent {
    data class SelectService(val service: TravelService) : ServicesIntent()
}

sealed class ServicesEffect : UiEffect {
    data class NavigateToBooking(val service: TravelService) : ServicesEffect()
}
