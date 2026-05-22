package com.travelmonk.core.common.config

/**
 * Write-only interface for triggering a feature flag sync.
 * Only the login/auth flow should call [sync]; all other code reads via [FeatureFlagStore].
 *
 * Statsig migration path: implement this interface with StatSigFeatureFlagSyncer,
 * update the @Binds in ConfigModule — zero UI changes required.
 */
interface FeatureFlagSyncer {
    suspend fun sync()
}
