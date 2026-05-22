package com.travelmonk.feature.onboarding.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState

@Immutable
data class WelcomeState(
    val isLoading: Boolean = false
) : UiState

sealed interface WelcomeIntent : UiIntent {
    data object GetStarted : WelcomeIntent
    data object Login : WelcomeIntent
}

sealed interface WelcomeEffect : UiEffect {
    data object NavigateToPhoneEntry : WelcomeEffect
    /** Emitted on init when a registered passkey is found — skips phone entry. */
    data object NavigateToPasskeyPrompt : WelcomeEffect
}
