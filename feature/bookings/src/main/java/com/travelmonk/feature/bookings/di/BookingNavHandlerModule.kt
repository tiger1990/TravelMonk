package com.travelmonk.feature.bookings.di

import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.feature.bookings.navigation.BookingNavKeyHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class BookingNavHandlerModule {

    @Binds
    @IntoSet
    abstract fun bindBookingHandler(impl: BookingNavKeyHandler): NavKeyHandler
}