package com.travelmonk.feature.home.di

import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.feature.home.navigation.HomeNavKeyHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeNavHandlerModule {

    @Binds
    @IntoSet
    abstract fun bindHomeHandler(impl: HomeNavKeyHandler): NavKeyHandler
}