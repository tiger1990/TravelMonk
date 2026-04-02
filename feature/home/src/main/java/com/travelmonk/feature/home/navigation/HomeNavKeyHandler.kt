package com.travelmonk.feature.home.navigation

import com.travelmonk.core.navigation.NavDestination
import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.homeapi.navigation.HomeNavKey
import javax.inject.Inject

class HomeNavKeyHandler @Inject constructor() : NavKeyHandler {
    override fun canHandle(key: TravelNavKey) = key is HomeNavKey
    override fun resolve(key: TravelNavKey) = NavDestination(key, NavTab.HOME)
}
