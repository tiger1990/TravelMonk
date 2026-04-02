package com.travelmonk.core.navigation

/**
 * Contract each feature team implements to participate in the navigation registry.
 *
 * A handler is responsible for two things:
 *  1. Claiming ownership of the key types it understands ([canHandle])
 *  2. Declaring which tab the key lives under and what to push ([resolve])
 *
 * Handlers are contributed to the app-layer [NavigationRegistry] via
 * Hilt multibindings (@Binds @IntoSet). Feature teams add one handler
 * per feature — the registry and NavigationState require no changes.
 */
interface NavKeyHandler {
    fun canHandle(key: TravelNavKey): Boolean
    fun resolve(key: TravelNavKey): NavDestination
}
