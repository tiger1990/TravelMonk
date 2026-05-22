package com.travelmonk.feature.onboarding.data.local

import kotlinx.serialization.Serializable

/**
 * Typed model for the encrypted session DataStore.
 *
 * All fields default to empty/false so [SessionDataSerializer] can return [EMPTY]
 * safely when the store has no data yet (first launch) or when decryption fails
 * (corrupted file — treated as "no session", user re-authenticates).
 *
 * [isAuthenticated] is the single source of truth for auth gate decisions.
 */
@Serializable
data class SessionData(
    val accessToken: String = "",
    val refreshToken: String = "",
    val userId: String = "",
    val phoneNumber: String = "",
    val passkeyRegistered: Boolean = false,
    /**
     * Set to true only after the user explicitly acts on PasskeyPrompt (Skip / Register / Sign-in).
     * Keeps [isAuthenticated] false until that decision is made, preventing the auth gate from
     * opening immediately after OTP — which would tear down onboarding before PasskeyPrompt shows.
     * Survives process death: if the OS kills the app on PasskeyPrompt, the token is preserved
     * here and the user only needs to re-do OTP, not re-enter credentials from scratch.
     */
    val onboardingComplete: Boolean = false
) {
    val isAuthenticated: Boolean
        get() = accessToken.isNotBlank() && userId.isNotBlank() && onboardingComplete

    companion object {
        val EMPTY = SessionData()
    }
}