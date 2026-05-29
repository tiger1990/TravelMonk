package com.travelmonk.feature.onboarding.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.common.ui.UiText
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.domain.usecase.SendOtpUseCase
import com.travelmonk.feature.onboarding.domain.usecase.VerifyOtpUseCase
import com.travelmonk.feature.onboarding.mvi.OtpEffect
import com.travelmonk.feature.onboarding.mvi.OtpIntent
import com.travelmonk.feature.onboarding.mvi.OtpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RESEND_COOLDOWN_SECONDS = 30
private const val KEY_RESEND_COOLDOWN = "resend_cooldown"

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
) : BaseViewModel<OtpState, OtpIntent, OtpEffect>(
    OtpState(
        resendCooldownSeconds = savedStateHandle.get<Int>(KEY_RESEND_COOLDOWN) ?: 0
    )
) {

    private var verifyJob: Job? = null
    private var resendJob: Job? = null
    private var cooldownJob: Job? = null

    init {
        // Resume countdown after process death (SavedStateHandle persists across process death)
        if (currentState.resendCooldownSeconds > 0) runCooldown(currentState.resendCooldownSeconds)
    }

    override fun handleIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.SetPhone -> setState { copy(phone = intent.phone) }
            is OtpIntent.OtpChanged -> {
                // Enforce digits-only in the ViewModel — keyboard type is a hint, not a guarantee
                if (intent.otp.all { it.isDigit() }) {
                    setState { copy(otp = intent.otp, error = null) }
                }
            }
            is OtpIntent.Submit -> verifyOtp()
            is OtpIntent.ResendOtp -> resendOtp()
        }
    }

    private fun verifyOtp() {
        verifyJob?.cancel()
        verifyJob = viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = verifyOtpUseCase(currentState.phone, currentState.otp)) {
                is DataResult.Success -> {
                    setState { copy(isLoading = false) }
                    setEffect(OtpEffect.NavigateToPasskeyPrompt)
                }
                is DataResult.Error -> setState {
                    copy(
                        isLoading = false,
                        error = result.message?.let { UiText.Raw(it) }
                            ?: UiText.Res(R.string.feature_onboarding_error_otp_verification_failed)
                    )
                }
                // suspend mutation — Loading is not a terminal value; required for exhaustive when.
                is DataResult.Loading -> Unit
            }
        }
    }

    private fun resendOtp() {
        if (currentState.resendCooldownSeconds > 0) return
        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            when (val result = sendOtpUseCase(currentState.phone)) {
                is DataResult.Success -> {
                    savedStateHandle[KEY_RESEND_COOLDOWN] = RESEND_COOLDOWN_SECONDS
                    setState { copy(resendCooldownSeconds = RESEND_COOLDOWN_SECONDS) }
                    runCooldown(RESEND_COOLDOWN_SECONDS)
                }
                is DataResult.Error -> setState {
                    copy(
                        error = result.message?.let { UiText.Raw(it) }
                            ?: UiText.Res(R.string.feature_onboarding_error_resend_failed)
                    )
                }
                // suspend mutation — Loading is not a terminal value; required for exhaustive when.
                is DataResult.Loading -> Unit
            }
        }
    }

    private fun runCooldown(from: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            repeat(from) {
                delay(1_000)
                val remaining = currentState.resendCooldownSeconds - 1
                savedStateHandle[KEY_RESEND_COOLDOWN] = remaining
                setState { copy(resendCooldownSeconds = remaining) }
            }
        }
    }
}
