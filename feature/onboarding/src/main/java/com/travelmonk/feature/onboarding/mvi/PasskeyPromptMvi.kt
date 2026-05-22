package com.travelmonk.feature.onboarding.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.core.common.ui.UiText

@Immutable
data class PasskeyPromptState(
    val isLoading: Boolean = false,
    val error: UiText? = null
) : UiState

sealed interface PasskeyPromptIntent : UiIntent {
    data object RegisterPasskey : PasskeyPromptIntent
    data object SignInWithPasskey : PasskeyPromptIntent
    data class AttestationObtained(val attestationJson: String) : PasskeyPromptIntent
    data class AssertionObtained(val assertionJson: String) : PasskeyPromptIntent
    data object Skip : PasskeyPromptIntent
    /** User cancelled the Credential Manager dialog or the transaction key biometric prompt. Clears loading; no error shown. */
    data object CredentialCancelled : PasskeyPromptIntent
    /** Credential Manager or transaction key signing failed with a non-cancellation error. Clears loading and surfaces [message]. */
    data class CredentialError(val message: String) : PasskeyPromptIntent
}

sealed interface PasskeyPromptEffect : UiEffect {
    data class LaunchPasskeyRegistration(val challengeJson: String) : PasskeyPromptEffect
    data class LaunchPasskeyAuthentication(val challengeJson: String) : PasskeyPromptEffect
}
