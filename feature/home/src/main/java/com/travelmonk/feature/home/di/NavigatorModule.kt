package com.travelmonk.feature.home.di

import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.feature.homeapi.navigator.HomeNavigator
import com.travelmonk.feature.transportapi.navigation.TransportNavKey
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
    fun provideHomeNavigator(bus: NavigationBus): HomeNavigator = object : HomeNavigator {
        override fun back() = bus.back()
        override fun navigateToSearch() = bus.navigate(TransportNavKey.Root)
    }
}
