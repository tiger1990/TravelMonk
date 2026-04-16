package com.travelmonk.feature.experiencesapi.navigation

import com.travelmonk.core.navigation.TravelNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ExperienceNavKey : TravelNavKey {
    @Serializable
    @SerialName("experience.root")
    data object Root : ExperienceNavKey

    @Serializable
    @SerialName("experience.details")
    data class Details(val experienceId: String) : ExperienceNavKey
}
