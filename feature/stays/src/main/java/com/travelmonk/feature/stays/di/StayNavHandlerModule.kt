package com.travelmonk.feature.stays.di

import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.feature.stays.navigation.StayNavKeyHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class StayNavHandlerModule {

    @Binds
    @IntoSet
    abstract fun bindStayHandler(impl: StayNavKeyHandler): NavKeyHandler
}