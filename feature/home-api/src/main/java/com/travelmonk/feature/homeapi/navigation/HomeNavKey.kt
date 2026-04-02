package com.travelmonk.feature.homeapi.navigation

import com.travelmonk.core.navigation.TravelNavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface HomeNavKey : TravelNavKey {
    @Serializable
    @SerialName("home.root")
    data object Root : HomeNavKey
}
