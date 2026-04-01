package com.travelmonk.feature.experiences.navigation

import com.travelmonk.core.navigation.TravelNavKey

sealed interface ExperienceNavKey : TravelNavKey {
    data object Root : ExperienceNavKey
    data class Details(val experienceId: String) : ExperienceNavKey
}
