package com.travelmonk.core.navigation

/**
 * Enum representing each bottom-navigation tab in the app.
 *
 * Used in [NavDestination] so feature handlers can declare which tab they belong to
 * without importing any other feature's nav keys.
 *
 * The mapping from NavTab → BottomBarItem lives in the app layer (NavigationState),
 * which is the only place that knows the full tab structure.
 */
enum class NavTab {
    HOME,
    TRANSPORT,
    STAYS,
    EXPERIENCES,
    SERVICES,
    BOOKINGS
}
