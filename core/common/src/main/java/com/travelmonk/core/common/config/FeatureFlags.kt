package com.travelmonk.core.common.config

/**
 * Runtime feature flags for controlling which bottom-bar tabs are visible.
 * Inject this interface to gate features for A/B testing or gradual rollout.
 * Home and Bookings are always enabled and intentionally excluded from this interface.
 */
interface FeatureFlags {
    val isTransportEnabled: Boolean
    val isStaysEnabled: Boolean
    val isExperiencesEnabled: Boolean
    val isServicesEnabled: Boolean
}
