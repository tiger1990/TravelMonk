package com.travelmonk.feature.onboarding.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.core.common.ui.UiText

@Immutable
data class OtpState(
    val phone: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val resendCooldownSeconds: Int = 0
) : UiState

sealed interface OtpIntent : UiIntent {
    // Retired: phone now flows via @AssistedInject from the nav key (see OtpViewModel).
    // data class SetPhone(val phone: String) : OtpIntent
    data class OtpChanged(val otp: String) : OtpIntent
    data object Submit : OtpIntent
    data object ResendOtp : OtpIntent
}

sealed interface OtpEffect : UiEffect {
    data object NavigateToPasskeyPrompt : OtpEffect
}
