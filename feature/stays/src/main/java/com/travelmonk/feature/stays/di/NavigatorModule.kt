package com.travelmonk.feature.stays.di

import com.travelmonk.core.model.BookingType
import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.staysapi.navigator.StayNavigator
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
    fun provideStayNavigator(bus: NavigationBus): StayNavigator = object : StayNavigator {
        override fun navigateTo(key: StayNavKey) = bus.navigate(key)
        override fun back() = bus.back()
        override fun navigateToStayDetail(stayId: String) = bus.navigate(StayNavKey.Details(stayId))
        override fun navigateToBookingConfirmation(type: BookingType, title: String) =
            bus.navigate(BookingNavKey.Confirmation(type, title))
    }
}
