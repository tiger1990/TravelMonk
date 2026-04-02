package com.travelmonk.navigation

import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.core.navigation.TravelNavKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface NavCommand {
    data class Navigate(val key: TravelNavKey) : NavCommand
    data object Back : NavCommand
}

/**
 * App-layer implementation of [NavigationBus].
 * Uses a SharedFlow to buffer navigation events, preventing "Event Loss" 
 * during configuration changes or when the app is in the background.
 */
@Singleton
class GlobalNavigator @Inject constructor() : NavigationBus {

    private val _navEvents = MutableSharedFlow<NavCommand>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navEvents = _navEvents.asSharedFlow()

    override fun navigate(key: TravelNavKey) {
        _navEvents.tryEmit(NavCommand.Navigate(key))
    }

    override fun back() {
        _navEvents.tryEmit(NavCommand.Back)
    }
}
