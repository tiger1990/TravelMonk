package com.travelmonk.feature.transport.navigation

import com.travelmonk.core.navigation.NavDestination
import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.transportapi.navigation.TransportNavKey
import javax.inject.Inject

class TransportNavKeyHandler @Inject constructor() : NavKeyHandler {
    override fun canHandle(key: TravelNavKey) = key is TransportNavKey
    override fun resolve(key: TravelNavKey) = NavDestination(key, NavTab.TRANSPORT)
}
