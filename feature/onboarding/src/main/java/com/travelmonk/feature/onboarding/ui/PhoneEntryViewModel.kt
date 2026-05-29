package com.travelmonk.feature.onboarding.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.common.ui.UiText
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.domain.usecase.SendOtpUseCase
import com.travelmonk.feature.onboarding.mvi.PhoneEntryEffect
import com.travelmonk.feature.onboarding.mvi.PhoneEntryIntent
import com.travelmonk.feature.onboarding.mvi.PhoneEntryState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneEntryViewModel @Inject constructor(
    private val sendOtpUseCase: SendOtpUseCase
) : BaseViewModel<PhoneEntryState, PhoneEntryIntent, PhoneEntryEffect>(PhoneEntryState()) {

    override fun handleIntent(intent: PhoneEntryIntent) {
        when (intent) {
            is PhoneEntryIntent.PhoneChanged -> setState { copy(phone = intent.phone, error = null) }
            is PhoneEntryIntent.Submit -> sendOtp()
        }
    }

    private fun sendOtp() {
        val phone = currentState.phone.trim()
        // E.164 validation: must start with +, have 7–15 digits (ITU-T E.164 standard)
        val digits = phone.filter { it.isDigit() }
        if (!phone.startsWith("+") || digits.length < 7 || digits.length > 15) {
            setState { copy(error = UiText.Res(R.string.feature_onboarding_error_invalid_phone)) }
            return
        }
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = sendOtpUseCase(phone)) {
                is DataResult.Success -> {
                    setState { copy(isLoading = false) }
                    setEffect(PhoneEntryEffect.NavigateToOtp(phone))
                }
                is DataResult.Error -> setState {
                    copy(
                        isLoading = false,
                        error = result.message?.let { UiText.Raw(it) }
                            ?: UiText.Res(R.string.feature_onboarding_error_send_otp_failed)
                    )
                }
                // suspend mutation — Loading is not a terminal value; required for exhaustive when.
                is DataResult.Loading -> Unit
            }
        }
    }
}
