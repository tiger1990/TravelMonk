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
    @Binds
    @IntoSet
    abstract fun bindBusTabContentProvider(
        provider: BusTabContentProvider
    ): TransportTabContentProvider

    @Binds
    @IntoSet
    abstract fun bindTrainTabContentProvider(
        provider: TrainTabContentProvider
    ): TransportTabContentProvider
}
