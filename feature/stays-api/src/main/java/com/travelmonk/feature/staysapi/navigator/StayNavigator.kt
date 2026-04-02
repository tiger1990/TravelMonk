package com.travelmonk.feature.staysapi.navigator

import androidx.compose.runtime.Stable
import com.travelmonk.feature.staysapi.navigation.StayNavKey

@Stable // All implementations are Hilt singletons; safe for Compose to skip recomposition
interface StayNavigator {
    fun navigateTo(key: StayNavKey)
    fun back()
}
