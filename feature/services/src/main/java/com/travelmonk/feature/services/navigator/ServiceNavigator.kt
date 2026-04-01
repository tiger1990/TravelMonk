package com.travelmonk.feature.services.navigator

import com.travelmonk.feature.services.navigation.ServiceNavKey

interface ServiceNavigator {
    fun navigateTo(key: ServiceNavKey)
    fun back()
    fun navigateToBookingConfirmation(type: String, title: String)
}
