package com.travelmonk.feature.stays.navigator

import com.travelmonk.feature.stays.navigation.StayNavKey

interface StayNavigator {
    fun navigateTo(key: StayNavKey)
    fun back()
}
