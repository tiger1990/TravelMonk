package com.travelmonk.feature.bookings.navigation

import com.travelmonk.core.navigation.NavDestination
import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import javax.inject.Inject

// All BookingNavKey variants (Root, Confirmation) resolve to the Bookings tab
class BookingNavKeyHandler @Inject constructor() : NavKeyHandler {
    override fun canHandle(key: TravelNavKey) = key is BookingNavKey
    override fun resolve(key: TravelNavKey) = NavDestination(key, NavTab.BOOKINGS)
}
