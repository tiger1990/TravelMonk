package com.travelmonk.di

import com.travelmonk.core.common.config.FeatureFlags
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureFlagsModule {

    @Provides
    @Singleton
    fun provideFeatureFlags(): FeatureFlags = object : FeatureFlags {
        override val isTransportEnabled: Boolean = true
        override val isStaysEnabled: Boolean = true
        override val isExperiencesEnabled: Boolean = true
        override val isServicesEnabled: Boolean = true
    }
}
