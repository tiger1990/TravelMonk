package com.travelmonk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.travelmonk.feature.onboarding.navigation.OnboardingNavAction
import com.travelmonk.feature.onboarding.navigation.OnboardingNavigationBus
import com.travelmonk.feature.onboarding.ui.OtpScreen
import com.travelmonk.feature.onboarding.ui.PasskeyPromptScreen
import com.travelmonk.feature.onboarding.ui.PhoneEntryScreen
import com.travelmonk.feature.onboarding.ui.WelcomeScreen
import com.travelmonk.feature.onboardingapi.navigation.OnboardingNavKey
import com.travelmonk.feature.onboardingapi.navigator.OnboardingNavigator

/**
 * Standalone onboarding nav host. Shown by [MainActivity] when the user is not authenticated.
 * Manages its own back stack — no tab bar, no [com.travelmonk.core.navigation.NavigationBus].
 *
 * Navigation events from ViewModels travel through [OnboardingNavigationBus] and are collected
 * here to push/pop entries on the local back stack.
 */
@Composable
fun OnboardingFlow(bus: OnboardingNavigationBus, navigator: OnboardingNavigator) {
    val backStack = rememberNavBackStack(OnboardingNavKey.Welcome)

    LaunchedEffect(bus) {
        bus.actions.collect { action ->
            when (action) {
                is OnboardingNavAction.Push -> backStack.add(action.key)
                is OnboardingNavAction.Back -> {
                    if (backStack.size > 1) backStack.removeLastOrNull()
                }
            }
        }
    }

    val saveableDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val viewModelDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()
    val decorators = remember(saveableDecorator, viewModelDecorator) {
        listOf(saveableDecorator, viewModelDecorator)
    }

    val provider = remember(navigator) {
        entryProvider {
            entry<OnboardingNavKey.Welcome> { WelcomeScreen(navigator) }
            entry<OnboardingNavKey.PhoneEntry> { PhoneEntryScreen(navigator) }
            entry<OnboardingNavKey.Otp> { key ->
                OtpScreen(navigator, phone = key.phone)
            }
            entry<OnboardingNavKey.PasskeyPrompt> { PasskeyPromptScreen() }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = decorators,
        entryProvider = provider as (NavKey) -> NavEntry<NavKey>
    )

    NavDisplay(
        entries = entries,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
    )
}
