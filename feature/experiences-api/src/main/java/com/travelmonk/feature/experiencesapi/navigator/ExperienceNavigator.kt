package com.travelmonk.feature.experiencesapi.navigator

import androidx.compose.runtime.Stable
import com.travelmonk.core.model.BookingType
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey

@Stable // All implementations are Hilt singletons; safe for Compose to skip recomposition
interface ExperienceNavigator {
    fun navigateTo(key: ExperienceNavKey)
    fun back()
    fun navigateToExperienceDetail(experienceId: String)
    fun navigateToBookingConfirmation(type: BookingType, title: String)
}
