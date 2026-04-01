package com.travelmonk.feature.services.navigation

import com.travelmonk.core.navigation.TravelNavKey

sealed interface ServiceNavKey : TravelNavKey {
    data object Root : ServiceNavKey
}
