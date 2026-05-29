package com.travelmonk.feature.onboarding.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.common.ui.UiText
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.data.local.UserSessionStore
import com.travelmonk.feature.onboarding.data.mock.PasskeyMockDataSource
import com.travelmonk.feature.onboarding.domain.repository.PasskeyRepository
import com.travelmonk.feature.onboarding.domain.usecase.PasskeyAuthUseCase
import com.travelmonk.feature.onboarding.domain.usecase.PasskeyRegistrationUseCase
import com.travelmonk.feature.onboarding.mvi.PasskeyPromptEffect
import com.travelmonk.feature.onboarding.mvi.PasskeyPromptIntent
import com.travelmonk.feature.onboarding.mvi.PasskeyPromptState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasskeyPromptViewModel @Inject constructor(
    private val passkeyRepository: PasskeyRepository,
    private val passkeyRegistrationUseCase: PasskeyRegistrationUseCase,
    private val passkeyAuthUseCase: PasskeyAuthUseCase,
    private val userSessionStore: UserSessionStore,
    private val mockDataSource: PasskeyMockDataSource
) : BaseViewModel<PasskeyPromptState, PasskeyPromptIntent, PasskeyPromptEffect>(PasskeyPromptState()) {

    private var passkeyJob: Job? = null

    override fun handleIntent(intent: PasskeyPromptIntent) {
        when (intent) {
            is PasskeyPromptIntent.RegisterPasskey -> beginRegistration()
            is PasskeyPromptIntent.SignInWithPasskey -> beginAuthentication()
            is PasskeyPromptIntent.AttestationObtained -> completeRegistration(intent.attestationJson)
            is PasskeyPromptIntent.AssertionObtained -> completeAuthentication(intent.assertionJson)
            is PasskeyPromptIntent.Skip -> viewModelScope.launch {
                userSessionStore.markOnboardingComplete()
                // authStateFlow → Authenticated; MainActivity transitions automatically
            }
            // Credential Manager / transaction key biometric was cancelled by the user.
            // Reset loading; no error — cancellation is an expected user action.
            is PasskeyPromptIntent.CredentialCancelled -> setState { copy(isLoading = false) }
            // Credential Manager or transaction key signing failed.
            is PasskeyPromptIntent.CredentialError -> setState {
                copy(isLoading = false, error = UiText.Raw(intent.message))
            }
        }
    }

    private fun beginRegistration() {
        passkeyJob?.cancel()
        passkeyJob = viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            val userId = userSessionStore.sessionFlow.value.userId
            when (val result = passkeyRepository.beginRegistration(userId)) {
                is DataResult.Success -> {
                    if (mockDataSource.isEnabled) {
                        // TODO: Remove this branch when backend is integrated
                        onIntent(PasskeyPromptIntent.AttestationObtained(mockDataSource.attestationResponseJson()))
                    } else {
                        // Keep isLoading = true — spinner stays on while the Screen handles
                        // CredentialManager + transaction key biometric. Loading is cleared by
                        // CredentialCancelled, CredentialError, or the subsequent AttestationObtained path.
                        setEffect(PasskeyPromptEffect.LaunchPasskeyRegistration(result.data))
                    }
                }
                is DataResult.Error -> setState {
                    copy(
                        isLoading = false,
                        error = result.message?.let { UiText.Raw(it) }
                            ?: UiText.Res(R.string.feature_onboarding_error_passkey_registration_start)
                    )
                }
                // suspend mutation — Loading is not a terminal value; required for exhaustive when.
                is DataResult.Loading -> Unit
            }
        }
    }

    private fun beginAuthentication() {
        passkeyJob?.cancel()
        passkeyJob = viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = passkeyRepository.beginAuthentication()) {
                is DataResult.Success -> {
                    if (mockDataSource.isEnabled) {
                        // TODO: Remove this branch when backend is integrated
                        onIntent(PasskeyPromptIntent.AssertionObtained(mockDataSource.assertionResponseJson()))
                    } else {
                        // Keep isLoading = true — same rationale as beginRegistration above.
                        setEffect(PasskeyPromptEffect.LaunchPasskeyAuthentication(result.data))
                    }
                }
                is DataResult.Error -> setState {
                    copy(
                        isLoading = false,
                        error = result.message?.let { UiText.Raw(it) }
                            ?: UiText.Res(R.string.feature_onboarding_error_passkey_auth_start)
                    )
                }
                // suspend mutation — Loading is not a terminal value; required for exhaustive when.
                is DataResult.Loading -> Unit
            }
        }
    }

    private fun completeRegistration(attestationJson: String) {
        passkeyJob?.cancel()
        passkeyJob = viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            val userId = userSessionStore.sessionFlow.value.userId
            when (val result = passkeyRegistrationUseCase(userId, attestationJson)) {
                is DataResult.Success -> {
                    // markPasskeyRegistered() already called inside PasskeyRegistrationUseCase
                    userSessionStore.markOnboardingComplete()
                    setState { copy(isLoading = false) }
                }
                is DataResult.Error -> setState {
                    copy(
                        isLoading = false,
                        error = result.message?.let { UiText.Raw(it) }
                            ?: UiText.Res(R.string.feature_onboarding_error_passkey_registration_failed)
                    )
                }
                // suspend mutation — Loading is not a terminal value; required for exhaustive when.
                is DataResult.Loading -> Unit
            }
        }
    }

    private fun completeAuthentication(assertionJson: String) {
        passkeyJob?.cancel()
        passkeyJob = viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = passkeyAuthUseCase(assertionJson)) {
                is DataResult.Success -> {
                    userSessionStore.markOnboardingComplete()
                    setState { copy(isLoading = false) }
                }
                is DataResult.Error -> setState {
                    copy(
                        isLoading = false,
                        error = result.message?.let { UiText.Raw(it) }
                            ?: UiText.Res(R.string.feature_onboarding_error_passkey_auth_failed)
                    )
                }
                // suspend mutation — Loading is not a terminal value; required for exhaustive when.
                is DataResult.Loading -> Unit
            }
        }
    }
}
