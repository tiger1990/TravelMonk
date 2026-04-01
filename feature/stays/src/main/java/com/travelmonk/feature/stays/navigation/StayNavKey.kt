package com.travelmonk.feature.stays.navigation

import com.travelmonk.core.navigation.TravelNavKey

sealed interface StayNavKey : TravelNavKey {
    data object Search : StayNavKey
    data class Results(val location: String) : StayNavKey
}
