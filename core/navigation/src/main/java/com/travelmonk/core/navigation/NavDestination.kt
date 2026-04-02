package com.travelmonk.core.navigation

/**
 * Resolved routing decision produced by a [NavKeyHandler].
 *
 * @param key    The nav key to push onto the back stack.
 * @param navTab The tab that owns this destination. NavigationState maps this to a
 *               BottomBarItem and switches tabs before pushing the key. Using [NavTab]
 *               instead of a raw TravelNavKey means handlers never need to import
 *               another feature's nav keys.
 */
data class NavDestination(
    val key: TravelNavKey,
    val navTab: NavTab
)
