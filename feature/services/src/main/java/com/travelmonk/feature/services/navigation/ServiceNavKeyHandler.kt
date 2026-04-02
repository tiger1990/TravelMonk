package com.travelmonk.feature.services.navigation

import com.travelmonk.core.navigation.NavDestination
import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey
import javax.inject.Inject

class ServiceNavKeyHandler @Inject constructor() : NavKeyHandler {
    override fun canHandle(key: TravelNavKey) = key is ServiceNavKey
    override fun resolve(key: TravelNavKey) = NavDestination(key, NavTab.SERVICES)
}
