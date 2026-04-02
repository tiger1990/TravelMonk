package com.travelmonk.feature.experiences.navigation

import com.travelmonk.core.navigation.NavDestination
import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey
import javax.inject.Inject

class ExperienceNavKeyHandler @Inject constructor() : NavKeyHandler {
    override fun canHandle(key: TravelNavKey) = key is ExperienceNavKey
    override fun resolve(key: TravelNavKey) = NavDestination(key, NavTab.EXPERIENCES)
}
