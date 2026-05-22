package com.travelmonk.feature.onboarding.navigation

import com.travelmonk.feature.onboardingapi.navigation.OnboardingNavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Internal navigation bus for the onboarding flow.
 *
 * Onboarding runs in a standalone nav host (pre-authentication), so it cannot
 * participate in the tab-based [com.travelmonk.core.navigation.NavigationBus].
 * ViewModels push [OnboardingNavAction] events; OnboardingFlow collects and
 * updates its own back stack.
 */
sealed interface OnboardingNavAction {
    data class Push(val key: OnboardingNavKey) : OnboardingNavAction
    data object Back : OnboardingNavAction
}

@Singleton
class OnboardingNavigationBus @Inject constructor() {

    private val _actions = MutableSharedFlow<OnboardingNavAction>(extraBufferCapacity = 1)
    val actions: SharedFlow<OnboardingNavAction> = _actions.asSharedFlow()

    fun push(key: OnboardingNavKey) {
        _actions.tryEmit(OnboardingNavAction.Push(key))
    }

    fun back() {
        _actions.tryEmit(OnboardingNavAction.Back)
    }
}
