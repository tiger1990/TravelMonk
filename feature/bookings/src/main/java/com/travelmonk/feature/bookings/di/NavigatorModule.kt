package com.travelmonk.feature.bookings.di

import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.bookingsapi.navigator.BookingNavigator
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
    fun provideBookingNavigator(bus: NavigationBus): BookingNavigator = object : BookingNavigator {
        override fun back() = bus.back()
        override fun navigateToMyBookings() = bus.navigate(
            BookingNavKey.Root
        )
    }
}
