package com.travelmonk.feature.home.navigation

import com.travelmonk.core.navigation.TravelNavKey

sealed interface HomeNavKey : TravelNavKey {
    data object Root : HomeNavKey
}
