package com.travelmonk.feature.transport.di

import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.feature.transport.navigation.TransportNavKeyHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TransportNavHandlerModule {

    @Binds
    @IntoSet
    abstract fun bindTransportHandler(impl: TransportNavKeyHandler): NavKeyHandler
}