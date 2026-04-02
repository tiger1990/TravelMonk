package com.travelmonk.feature.servicesapi.navigator

import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey

interface ServiceNavigator {
    fun navigateTo(key: ServiceNavKey)
    fun back()
    fun navigateToBookingConfirmation(type: String, title: String)
}
