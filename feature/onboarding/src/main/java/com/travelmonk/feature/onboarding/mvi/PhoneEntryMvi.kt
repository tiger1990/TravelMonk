package com.travelmonk.feature.onboarding.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.core.common.ui.UiText

@Immutable
data class PhoneEntryState(
    val phone: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null
) : UiState

sealed interface PhoneEntryIntent : UiIntent {
    data class PhoneChanged(val phone: String) : PhoneEntryIntent
    data object Submit : PhoneEntryIntent
}

sealed interface PhoneEntryEffect : UiEffect {
    data class NavigateToOtp(val phone: String) : PhoneEntryEffect
}
