package com.travelmonk.feature.onboarding.di

import com.travelmonk.feature.onboarding.navigation.OnboardingNavigationBus
import com.travelmonk.feature.onboardingapi.navigation.OnboardingNavKey
import com.travelmonk.feature.onboardingapi.navigator.OnboardingNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigatorModule {

    @Provides
    @Singleton
    fun provideOnboardingNavigator(bus: OnboardingNavigationBus): OnboardingNavigator =
        object : OnboardingNavigator {
            override fun toPhoneEntry() = bus.push(OnboardingNavKey.PhoneEntry)
            override fun toOtp(phone: String) = bus.push(OnboardingNavKey.Otp(phone))
            override fun toPasskeyPrompt() = bus.push(OnboardingNavKey.PasskeyPrompt)
            override fun back() = bus.back()
        }
}
