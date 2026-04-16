package com.travelmonk.feature.transport.di

import androidx.annotation.Keep
import com.travelmonk.feature.transportapi.TransportTabContentProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for accessing Transport tab providers within the UI layer.
 *
 * We use an EntryPoint here instead of ViewModel injection because these providers
 * contain @Composable content. This keeps the ViewModel pure and free of UI-layer
 * dependencies (Separation of Concerns).
 *
 * @Keep is required because this interface is accessed via reflection (EntryPointAccessors).
 */
@Keep
@EntryPoint
@InstallIn(SingletonComponent::class)
interface TransportTabProviderEntryPoint {
    fun tabProviders(): Set<@JvmSuppressWildcards TransportTabContentProvider>
}
