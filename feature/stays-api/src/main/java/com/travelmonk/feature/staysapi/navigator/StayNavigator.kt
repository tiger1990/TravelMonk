package com.travelmonk.feature.staysapi.navigator

import com.travelmonk.feature.staysapi.navigation.StayNavKey

interface StayNavigator {
    fun navigateTo(key: StayNavKey)
    fun back()
}
