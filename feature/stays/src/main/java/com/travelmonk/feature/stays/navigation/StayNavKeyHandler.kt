package com.travelmonk.feature.stays.navigation

import com.travelmonk.core.navigation.NavDestination
import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import javax.inject.Inject

class StayNavKeyHandler @Inject constructor() : NavKeyHandler {
    override fun canHandle(key: TravelNavKey) = key is StayNavKey
    override fun resolve(key: TravelNavKey) = NavDestination(key, NavTab.STAYS)
}
