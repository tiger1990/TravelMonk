package com.travelmonk.core.navigation

/**
 * Encodes navigation modifiers — mirrors the NavOptions pattern from Navigation Component,
 * React Navigation, and Flutter's Navigator 2.0.
 *
 * Callers declare *what* they want; [NavigationState] executes *how*.
 *
 * @param popCurrentTabToRoot When true, clears the active tab's back stack to its root
 *   before navigating. Use for terminal cross-tab actions (e.g. booking confirmation)
 *   so the source tab is clean when the user returns to it.
 * @param singleTop When true, skips the push if the key is already at the top of the
 *   destination stack (prevents duplicate entries from rapid taps).
 */
data class NavOptions(
    val popCurrentTabToRoot: Boolean = false,
    val singleTop: Boolean = false,
) {
    companion object {
        val Default = NavOptions()
    }
}