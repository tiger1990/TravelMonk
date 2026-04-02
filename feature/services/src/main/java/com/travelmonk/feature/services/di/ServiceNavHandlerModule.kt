package com.travelmonk.feature.services.di

import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.feature.services.navigation.ServiceNavKeyHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceNavHandlerModule {

    @Binds
    @IntoSet
    abstract fun bindServiceHandler(impl: ServiceNavKeyHandler): NavKeyHandler
}