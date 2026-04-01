package com.travelmonk.navigation

import com.travelmonk.feature.flights.navigation.FlightNavKey
import com.travelmonk.feature.flights.navigator.FlightNavigator
import com.travelmonk.feature.stays.navigation.StayNavKey
import com.travelmonk.feature.stays.navigator.StayNavigator
import com.travelmonk.feature.services.navigation.ServiceNavKey
import com.travelmonk.feature.services.navigator.ServiceNavigator
import com.travelmonk.feature.experiences.navigation.ExperienceNavKey
import com.travelmonk.feature.experiences.navigator.ExperienceNavigator
import com.travelmonk.feature.bookings.navigation.BookingNavKey
import com.travelmonk.feature.home.navigator.HomeNavigator
import com.travelmonk.feature.transport.navigation.TransportNavKey
import com.travelmonk.ui.navigation.NavigationState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalNavigator @Inject constructor() :
    FlightNavigator, StayNavigator, ServiceNavigator, ExperienceNavigator, HomeNavigator {

    private var navigationState: NavigationState? = null

    fun bind(navigationState: NavigationState) {
        this.navigationState = navigationState
    }

    fun unbind() {
        this.navigationState = null
    }

    override fun navigateTo(key: FlightNavKey) {
        navigationState?.navigateTo(key)
    }

    override fun navigateTo(key: StayNavKey) {
        navigationState?.navigateTo(key)
    }

    override fun navigateTo(key: ServiceNavKey) {
        navigationState?.navigateTo(key)
    }

    override fun navigateTo(key: ExperienceNavKey) {
        navigationState?.navigateTo(key)
    }

    override fun back() {
        navigationState?.pop()
    }

    override fun navigateToBookingConfirmation(type: String, title: String) {
        navigationState?.navigateTo(BookingNavKey.Confirmation(type, title))
    }

    override fun navigateToSearch() {
        navigationState?.navigateTo(TransportNavKey.Root)
    }
}
