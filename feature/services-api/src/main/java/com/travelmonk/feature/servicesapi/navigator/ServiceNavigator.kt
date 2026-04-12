package com.travelmonk.feature.servicesapi.navigator

import androidx.compose.runtime.Stable
import com.travelmonk.core.model.BookingType
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey

@Stable // All implementations are Hilt singletons; safe for Compose to skip recomposition
interface ServiceNavigator {
    fun navigateTo(key: ServiceNavKey)
    fun back()
    fun navigateToBookingConfirmation(type: BookingType, title: String)
}
