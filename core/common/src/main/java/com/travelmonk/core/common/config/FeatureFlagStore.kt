package com.travelmonk.core.common.config

import kotlinx.coroutines.flow.StateFlow

/**
 * Read-only reactive interface for feature flags.
 * Collect [flagsFlow] in the UI layer to react to flag changes without restart.
 */
interface FeatureFlagStore {
    val flagsFlow: StateFlow<FeatureFlagsData>
}
