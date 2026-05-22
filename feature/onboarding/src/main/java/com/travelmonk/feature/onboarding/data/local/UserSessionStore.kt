package com.travelmonk.feature.onboarding.data.local

import androidx.datastore.core.DataStore
import com.travelmonk.core.common.di.ApplicationScope
import com.travelmonk.feature.onboarding.domain.model.AuthState
import com.travelmonk.feature.onboarding.domain.model.AuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the authenticated user session.
 *
 * Backed by an AEAD-encrypted [DataStore] — all fields are encrypted at rest via
 * Tink AES256-GCM before being written to disk (see [SessionDataSerializer]).
 * The master key lives in the Android Keystore (StrongBox on API 33+).
 *
 * Public API is intentionally narrow:
 * - [authStateFlow] — auth gate in MainActivity
 * - [sessionFlow]   — full session data for callers that need more than auth state
 * - [saveSession]   — called after successful OTP or passkey authentication
 * - [markPasskeyRegistered] — called after passkey creation
 * - [clearSession]  — called on logout
 */
@Singleton
class UserSessionStore @Inject constructor(
    private val dataStore: DataStore<SessionData>,
    @ApplicationScope private val appScope: CoroutineScope
) {
    /** Full typed session, eagerly shared — `.value` is always current after init. */
    val sessionFlow: StateFlow<SessionData> = dataStore.data
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionData.EMPTY
        )

    /** Auth gate — MainActivity observes this to switch between Onboarding and TravelMonkApp. */
    val authStateFlow: StateFlow<AuthState> = dataStore.data
        .map { session ->
            if (session.isAuthenticated) AuthState.Authenticated(session.userId)
            else AuthState.Unauthenticated
        }
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = AuthState.Loading
        )

    /** Synchronous passkey check backed by the eagerly-started [sessionFlow]. */
    fun isPasskeyRegistered(): Boolean = sessionFlow.value.passkeyRegistered

    suspend fun saveSession(token: AuthToken) {
        dataStore.updateData { current ->
            current.copy(
                accessToken  = token.accessToken,
                refreshToken = token.refreshToken,
                userId       = token.userId.ifBlank { current.userId },
                phoneNumber  = token.phoneNumber.ifBlank { current.phoneNumber }
            )
        }
    }

    suspend fun markPasskeyRegistered() {
        dataStore.updateData { it.copy(passkeyRegistered = true) }
    }

    /** Called when user completes PasskeyPrompt (Skip / Register / Sign-in). Opens the auth gate. */
    suspend fun markOnboardingComplete() {
        dataStore.updateData { it.copy(onboardingComplete = true) }
    }

    suspend fun clearSession() {
        dataStore.updateData { SessionData.EMPTY }
    }
}
