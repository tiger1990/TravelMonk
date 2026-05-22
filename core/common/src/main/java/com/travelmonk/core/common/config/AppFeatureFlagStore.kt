package com.travelmonk.core.common.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.travelmonk.core.common.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of feature flag interfaces, backed by DataStore.
 *
 * Implements three separate interfaces to enforce separation of concerns:
 * - [FeatureFlags]      — plain Boolean properties for backward-compat code
 * - [FeatureFlagStore]  — reactive [StateFlow] for Compose UI (collect → recompose)
 * - [FeatureFlagSyncer] — write-only sync trigger called once after login
 *
 * Statsig migration: replace this binding in ConfigModule with StatSigFeatureFlagStore
 * implementing the same three interfaces — zero changes to UI or use-case code.
 *
 * Got warning over ApplicationScope: so changed to  @param:ApplicationScope
 * This annotation is currently applied to the value parameter only, but in the future it will also be applied to property.
 * - To opt in to applying to both value parameter and property, add '-Xannotation-default-target=param-property' to your compiler arguments.
 * - To keep applying to the value parameter only, use the '@param:' annotation target.
 * See https://youtrack.jetbrains.com/issue/KT-73255 for more details.
 */
@Singleton
class AppFeatureFlagStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:ApplicationScope private val appScope: CoroutineScope
) : FeatureFlags, FeatureFlagStore, FeatureFlagSyncer {

    private object Keys {
        val TRANSPORT   = booleanPreferencesKey("flag_transport")
        val STAYS       = booleanPreferencesKey("flag_stays")
        val EXPERIENCES = booleanPreferencesKey("flag_experiences")
        val SERVICES    = booleanPreferencesKey("flag_services")
    }

    // Hot flow that survives configuration changes and is shared across the app
    override val flagsFlow: StateFlow<FeatureFlagsData> = dataStore.data
        .map { prefs ->
            FeatureFlagsData(
                isTransportEnabled   = prefs[Keys.TRANSPORT]   ?: true,
                isStaysEnabled       = prefs[Keys.STAYS]       ?: true,
                isExperiencesEnabled = prefs[Keys.EXPERIENCES] ?: true,
                isServicesEnabled    = prefs[Keys.SERVICES]    ?: true
            )
        }
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = FeatureFlagsData.DEFAULT
        )

    // FeatureFlags — plain Boolean delegates backed by the hot StateFlow
    override val isTransportEnabled:   Boolean get() = flagsFlow.value.isTransportEnabled
    override val isStaysEnabled:       Boolean get() = flagsFlow.value.isStaysEnabled
    override val isExperiencesEnabled: Boolean get() = flagsFlow.value.isExperiencesEnabled
    override val isServicesEnabled:    Boolean get() = flagsFlow.value.isServicesEnabled

    /**
     * Sync flags from remote API after login.
     * Until a real backend exists this persists current/default values — preventing cold-start flicker.
     * TODO: Replace with real API call when backend is integrated:
     *   val response = flagsApi.getFlags()
     *   dataStore.edit { prefs ->
     *       prefs[Keys.TRANSPORT]   = response.isTransportEnabled
     *       prefs[Keys.STAYS]       = response.isStaysEnabled
     *       prefs[Keys.EXPERIENCES] = response.isExperiencesEnabled
     *       prefs[Keys.SERVICES]    = response.isServicesEnabled
     *   }
     */
    override suspend fun sync() {
        dataStore.edit { prefs ->
            if (!prefs.contains(Keys.TRANSPORT))   prefs[Keys.TRANSPORT]   = true
            if (!prefs.contains(Keys.STAYS))       prefs[Keys.STAYS]       = true
            if (!prefs.contains(Keys.EXPERIENCES)) prefs[Keys.EXPERIENCES] = true
            if (!prefs.contains(Keys.SERVICES))    prefs[Keys.SERVICES]    = true
        }
    }
}
