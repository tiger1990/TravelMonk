package com.travelmonk.core.common.config

/**
 * Immutable snapshot of all feature flags.
 * Used as the value type emitted by [FeatureFlagStore.flagsFlow].
 *
 * Because this is a data class (value semantics), Compose's [remember] key
 * comparison works correctly — a new emission with changed values triggers
 * recomposition even though the store singleton reference never changes.
 */
data class FeatureFlagsData(
    val isTransportEnabled: Boolean = true,
    val isStaysEnabled: Boolean = true,
    val isExperiencesEnabled: Boolean = true,
    val isServicesEnabled: Boolean = true
) {
    companion object {
        val DEFAULT = FeatureFlagsData()
    }
}
