package com.travelmonk.core.common.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.travelmonk.core.common.config.AppFeatureFlagStore
import com.travelmonk.core.common.config.FeatureFlags
import com.travelmonk.core.common.config.FeatureFlagStore
import com.travelmonk.core.common.config.FeatureFlagSyncer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.featureFlagDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "feature_flags")

@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigModule {

    @Binds
    @Singleton
    abstract fun bindFeatureFlags(impl: AppFeatureFlagStore): FeatureFlags

    @Binds
    @Singleton
    abstract fun bindFeatureFlagStore(impl: AppFeatureFlagStore): FeatureFlagStore

    @Binds
    @Singleton
    abstract fun bindFeatureFlagSyncer(impl: AppFeatureFlagStore): FeatureFlagSyncer

    companion object {
        @Provides
        @Singleton
        fun provideFeatureFlagDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = context.featureFlagDataStore
    }
}
