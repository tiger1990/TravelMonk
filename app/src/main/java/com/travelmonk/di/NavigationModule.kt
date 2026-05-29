package com.travelmonk.di

import com.travelmonk.core.model.BookingType
import com.travelmonk.core.navigation.NavOptions
import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator
import com.travelmonk.feature.flightsapi.navigation.FlightNavKey
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator
import com.travelmonk.feature.homeapi.navigator.HomeNavigator
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey
import com.travelmonk.feature.servicesapi.navigator.ServiceNavigator
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.staysapi.navigator.StayNavigator
import com.travelmonk.feature.transportapi.navigation.TransportNavKey
import com.travelmonk.navigation.GlobalNavigator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Wires all navigator implementations in the app layer — the only module that
 * legitimately depends on all feature API modules. Cross-feature navigation
 * (e.g. booking confirmation from flights/stays/experiences/services) is resolved
 * here, keeping feature modules free of other features' nav key imports.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindNavigationBus(globalNavigator: GlobalNavigator): NavigationBus

    companion object {

        @Provides
        @Singleton
        fun provideHomeNavigator(bus: NavigationBus): HomeNavigator = object : HomeNavigator {
            override fun back() = bus.back()
            override fun navigateToSearch() = bus.navigate(TransportNavKey.Root)
        }

        @Provides
        @Singleton
        fun provideFlightNavigator(bus: NavigationBus): FlightNavigator = object : FlightNavigator {
            override fun navigateTo(key: FlightNavKey) = bus.navigate(key)
            override fun back() = bus.back()
            override fun navigateToBookingConfirmation(type: BookingType, title: String) =
                bus.navigate(BookingNavKey.Confirmation(type, title), NavOptions(popCurrentTabToRoot = true))
        }

        @Provides
        @Singleton
        fun provideExperienceNavigator(bus: NavigationBus): ExperienceNavigator = object : ExperienceNavigator {
            override fun navigateTo(key: ExperienceNavKey) = bus.navigate(key)
            override fun back() = bus.back()
            override fun navigateToExperienceDetail(experienceId: String) =
                bus.navigate(ExperienceNavKey.Details(experienceId))
            override fun navigateToBookingConfirmation(type: BookingType, title: String) =
                bus.navigate(BookingNavKey.Confirmation(type, title), NavOptions(popCurrentTabToRoot = true))
        }

        @Provides
        @Singleton
        fun provideStayNavigator(bus: NavigationBus): StayNavigator = object : StayNavigator {
            override fun navigateTo(key: StayNavKey) = bus.navigate(key)
            override fun back() = bus.back()
            override fun navigateToStayDetail(stayId: String) = bus.navigate(StayNavKey.Details(stayId))
            override fun navigateToBookingConfirmation(type: BookingType, title: String) =
                bus.navigate(BookingNavKey.Confirmation(type, title), NavOptions(popCurrentTabToRoot = true))
        }

        @Provides
        @Singleton
        fun provideServiceNavigator(bus: NavigationBus): ServiceNavigator = object : ServiceNavigator {
            override fun navigateTo(key: ServiceNavKey) = bus.navigate(key)
            override fun back() = bus.back()
            override fun navigateToBookingConfirmation(type: BookingType, title: String) =
                bus.navigate(BookingNavKey.Confirmation(type, title), NavOptions(popCurrentTabToRoot = true))
        }
    }
}
