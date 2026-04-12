package com.travelmonk.di

import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.navigation.GlobalNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindNavigationBus(globalNavigator: GlobalNavigator): NavigationBus
}
