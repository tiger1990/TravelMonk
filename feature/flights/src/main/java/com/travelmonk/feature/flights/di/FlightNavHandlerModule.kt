package com.travelmonk.feature.flights.di

import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.feature.flights.navigation.FlightNavKeyHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class FlightNavHandlerModule {

    @Binds
    @IntoSet
    abstract fun bindFlightHandler(impl: FlightNavKeyHandler): NavKeyHandler
}