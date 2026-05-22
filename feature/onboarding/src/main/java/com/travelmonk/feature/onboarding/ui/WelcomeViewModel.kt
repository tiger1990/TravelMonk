package com.travelmonk.feature.onboarding.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.feature.onboarding.data.local.UserSessionStore
import com.travelmonk.feature.onboarding.mvi.WelcomeEffect
import com.travelmonk.feature.onboarding.mvi.WelcomeIntent
import com.travelmonk.feature.onboarding.mvi.WelcomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val userSessionStore: UserSessionStore
) : BaseViewModel<WelcomeState, WelcomeIntent, WelcomeEffect>(WelcomeState()) {

    init {
        checkExistingSession()
    }

    override fun handleIntent(intent: WelcomeIntent) {
        when (intent) {
            is WelcomeIntent.GetStarted,
            is WelcomeIntent.Login -> viewModelScope.launch {
                setEffect(WelcomeEffect.NavigateToPhoneEntry)
            }
        }
    }

    /**
     * Checks the session on launch. If the user previously registered a passkey,
     * skip phone entry and navigate directly to passkey re-authentication.
     *
     * The Welcome screen is only shown when isAuthenticated = false (guaranteed by
     * MainActivity), so passkeyRegistered = true here always means a returning user
     * who needs to re-auth — never a new registration.
     *
     * sessionFlow is SharingStarted.Eagerly so .value is always current with no I/O wait.
     */
    private fun checkExistingSession() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val session = userSessionStore.sessionFlow.value
            setState { copy(isLoading = false) }
            if (session.passkeyRegistered) {
                setEffect(WelcomeEffect.NavigateToPasskeyPrompt)
            }
        }
    }
}
