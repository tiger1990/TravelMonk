package com.travelmonk.feature.services.di

import com.travelmonk.core.model.BookingType
import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey
import com.travelmonk.feature.servicesapi.navigator.ServiceNavigator
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
    fun provideServiceNavigator(bus: NavigationBus): ServiceNavigator = object : ServiceNavigator {
        override fun navigateTo(key: ServiceNavKey) = bus.navigate(key)
        override fun back() = bus.back()
        override fun navigateToBookingConfirmation(
            type: BookingType,
            title: String
        ) {
            bus.navigate(BookingNavKey.Confirmation(type, title))
        }
    }
}
