package com.travelmonk.core.testing.di

import com.travelmonk.core.common.config.DefaultFeatureFlags
import com.travelmonk.core.common.config.FeatureFlags
import com.travelmonk.core.common.config.FeatureFlagsData
import com.travelmonk.core.common.config.FeatureFlagStore
import com.travelmonk.core.common.config.FeatureFlagSyncer
import com.travelmonk.core.common.di.ConfigModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

/**
 * Replaces [ConfigModule] in all @HiltAndroidTest classes.
 *
 * Provides:
 * - [FeatureFlags]      → [DefaultFeatureFlags] (all features enabled, static)
 * - [FeatureFlagStore]  → [FakeFeatureFlagStore] (in-memory MutableStateFlow, no DataStore)
 * - [FeatureFlagSyncer] → [FakeFeatureFlagSyncer] no-op (avoids network calls in tests)
 *
 * Usage — just annotate your test with @HiltAndroidTest:
 *   @HiltAndroidTest
 *   @RunWith(AndroidJUnit4::class)
 *   class MyTest {
 *       @get:Rule val hiltRule = HiltAndroidRule(this)
 *   }
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ConfigModule::class]
)
object FakeFeatureFlagsModule {

    @Provides
    @Singleton
    fun provideFeatureFlags(): FeatureFlags = DefaultFeatureFlags

    @Provides
    @Singleton
    fun provideFeatureFlagStore(): FeatureFlagStore = FakeFeatureFlagStore()

    @Provides
    @Singleton
    fun provideFeatureFlagSyncer(): FeatureFlagSyncer = FakeFeatureFlagSyncer()
}

/** In-memory [FeatureFlagStore] for tests. Mutate [flow] directly to simulate flag changes. */
class FakeFeatureFlagStore(
    initialFlags: FeatureFlagsData = FeatureFlagsData.DEFAULT
) : FeatureFlagStore {
    val flow = MutableStateFlow(initialFlags)
    override val flagsFlow: StateFlow<FeatureFlagsData> = flow
}

/** No-op [FeatureFlagSyncer] — tracks call count for assertion in tests. */
class FakeFeatureFlagSyncer : FeatureFlagSyncer {
    var syncCallCount = 0
        private set

    override suspend fun sync() {
        syncCallCount++
    }
}
