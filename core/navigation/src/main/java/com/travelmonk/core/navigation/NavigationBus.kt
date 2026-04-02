package com.travelmonk.core.navigation

/**
 * Single navigation channel used by every feature.
 * Features push a typed [TravelNavKey] — the app-layer implementation resolves
 * which tab stack to push onto. Adding a new feature requires only one
 * @Provides adapter in NavigationModule; no changes here.
 */
interface NavigationBus {
    fun navigate(key: TravelNavKey)
    fun back()
}
