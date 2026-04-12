package com.travelmonk.feature.flights.di

import com.travelmonk.core.model.BookingType
import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.flightsapi.navigation.FlightNavKey
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator
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
    fun provideFlightNavigator(bus: NavigationBus): FlightNavigator = object : FlightNavigator {
        override fun navigateTo(key: FlightNavKey) = bus.navigate(key)
        override fun back() = bus.back()
        override fun navigateToBookingConfirmation(type: BookingType, title: String) =
            bus.navigate(BookingNavKey.Confirmation(type, title))
    }
}
