package com.travelmonk.feature.onboardingapi.navigation

import com.travelmonk.core.navigation.TravelNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface OnboardingNavKey : TravelNavKey {

    @Serializable
    @SerialName("onboarding.welcome")
    data object Welcome : OnboardingNavKey

    @Serializable
    @SerialName("onboarding.phone_entry")
    data object PhoneEntry : OnboardingNavKey

    @Serializable
    @SerialName("onboarding.otp")
    data class Otp(val phone: String) : OnboardingNavKey

    @Serializable
    @SerialName("onboarding.passkey_prompt")
    data object PasskeyPrompt : OnboardingNavKey
}
