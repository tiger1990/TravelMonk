package com.travelmonk.di

import com.travelmonk.core.common.config.FeatureFlags
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Staging feature flags — use this source set to gate features under staged rollout.
 * Flip any flag to false to hide that tab for staging testers before full production release.
 */
@Module
@InstallIn(SingletonComponent::class)
object FeatureFlagsModule {

    @Provides
    @Singleton
    fun provideFeatureFlags(): FeatureFlags = object : FeatureFlags {
        override val isTransportEnabled: Boolean = true
        override val isStaysEnabled: Boolean = true
        // Disable these two in staging — not yet ready for QA sign-off
        override val isExperiencesEnabled: Boolean = false
        override val isServicesEnabled: Boolean = false
    }
}
