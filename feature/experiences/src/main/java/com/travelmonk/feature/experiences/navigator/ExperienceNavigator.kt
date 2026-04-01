package com.travelmonk.feature.experiences.navigator

import com.travelmonk.feature.experiences.navigation.ExperienceNavKey

interface ExperienceNavigator {
    fun navigateTo(key: ExperienceNavKey)
    fun back()
    fun navigateToBookingConfirmation(type: String, title: String)
}
