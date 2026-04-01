package com.travelmonk.feature.services.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState

data class ServicesState(
    val isLoading: Boolean = false,
    val selectedService: TravelService? = null
) : UiState

data class TravelService(
    val id: String,
    val name: String,
    val iconName: String, // String identifier for the icon to keep domain decoupled from UI
    val description: String
)

sealed class ServicesIntent : UiIntent {
    data class SelectService(val service: TravelService) : ServicesIntent()
}

sealed class ServicesEffect : UiEffect {
    data class NavigateToBooking(val service: TravelService) : ServicesEffect()
}
