package com.travelmonk.feature.onboardingapi.navigator

import androidx.compose.runtime.Stable

/**
 * Public navigation contract for the onboarding flow.
 * ViewModels depend on this interface; the implementation lives in feature:onboarding.
 */
@Stable
interface OnboardingNavigator {
    fun toPhoneEntry()
    fun toOtp(phone: String)
    fun toPasskeyPrompt()
    fun back()
}
