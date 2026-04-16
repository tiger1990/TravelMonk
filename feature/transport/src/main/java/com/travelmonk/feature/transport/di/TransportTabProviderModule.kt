package com.travelmonk.feature.transport.di

import com.travelmonk.feature.transportapi.TransportTabContentProvider
import com.travelmonk.feature.transport.provider.BusTabContentProvider
import com.travelmonk.feature.transport.provider.TrainTabContentProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class TransportTabProviderModule {
    /**
     * Contributes [BusTabContentProvider] to the [TransportTabContentProvider] multibinding set.
     * Resolved by Hilt/KSP at compile time — no direct call site exists in source.
     */
    @Suppress("unused")
    @Binds
    @IntoSet
    abstract fun bindBusTabContentProvider(
        provider: BusTabContentProvider
    ): TransportTabContentProvider

    /**
     * Contributes [TrainTabContentProvider] to the [TransportTabContentProvider] multibinding set.
     * Resolved by Hilt/KSP at compile time — no direct call site exists in source.
     */
    @Suppress("unused")
    @Binds
    @IntoSet
    abstract fun bindTrainTabContentProvider(
        provider: TrainTabContentProvider
    ): TransportTabContentProvider
}
