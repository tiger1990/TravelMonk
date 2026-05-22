package com.travelmonk.core.common.config

/**
 * Runtime feature flags for controlling which bottom-bar tabs are visible.
 * Home and Bookings are always enabled and intentionally excluded from this interface.
 *
 * Performance: The FeatureFlags interface is marked as stable
 * in compose_stability_config.conf, ensuring that the UI
 * remains fast and skips unnecessary recompositions.
 */
interface FeatureFlags {
    val isTransportEnabled: Boolean
    val isStaysEnabled: Boolean
    val isExperiencesEnabled: Boolean
    val isServicesEnabled: Boolean
}

/**
 * Static defaults used during app startup or when the user is not logged in.
 */
object DefaultFeatureFlags : FeatureFlags {
    override val isTransportEnabled = true
    override val isStaysEnabled = true
    override val isExperiencesEnabled = true
    override val isServicesEnabled = true
}
