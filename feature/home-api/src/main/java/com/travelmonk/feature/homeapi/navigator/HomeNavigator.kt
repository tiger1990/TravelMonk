package com.travelmonk.feature.homeapi.navigator

import androidx.compose.runtime.Stable

@Stable // All implementations are Hilt singletons; safe for Compose to skip recomposition
interface HomeNavigator {
    fun back()
    fun navigateToSearch()
}
