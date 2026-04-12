package com.travelmonk.feature.transport.di

import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.feature.transportapi.navigator.TransportNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigatorModule {

    @Provides
    @Singleton
    fun provideTransportNavigator(bus: NavigationBus): TransportNavigator =
        object : TransportNavigator {
            override fun back() = bus.back()
        }
}
