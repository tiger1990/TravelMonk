package com.travelmonk.feature.experiencesapi.navigator

import androidx.compose.runtime.Stable
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey

@Stable // All implementations are Hilt singletons; safe for Compose to skip recomposition
interface ExperienceNavigator {
    fun navigateTo(key: ExperienceNavKey)
    fun back()
    fun navigateToBookingConfirmation(type: String, title: String)
}
