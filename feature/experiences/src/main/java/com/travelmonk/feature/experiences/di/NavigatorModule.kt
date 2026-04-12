package com.travelmonk.feature.experiences.di

import com.travelmonk.core.model.BookingType
import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator
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
    fun provideExperienceNavigator(bus: NavigationBus): ExperienceNavigator = object : ExperienceNavigator {
        override fun navigateTo(key: ExperienceNavKey) = bus.navigate(key)
        override fun back() = bus.back()
        override fun navigateToBookingConfirmation(type: BookingType, title: String) =
            bus.navigate(BookingNavKey.Confirmation(type, title))
    }
}
