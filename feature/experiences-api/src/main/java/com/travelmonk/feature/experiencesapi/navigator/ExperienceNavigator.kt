package com.travelmonk.feature.experiencesapi.navigator

import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey

interface ExperienceNavigator {
    fun navigateTo(key: ExperienceNavKey)
    fun back()
    fun navigateToBookingConfirmation(type: String, title: String)
}
