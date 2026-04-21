package com.travelmonk.navigation

import com.travelmonk.core.navigation.NavigationBus
import com.travelmonk.core.navigation.TravelNavKey
import androidx.compose.runtime.Stable
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
 *
 * Navigation commands are user-initiated, exactly-once actions. The buffer strategy
 * is intentionally [BufferOverflow.DROP_LATEST]:
 *
 * - [BufferOverflow.DROP_LATEST] drops the newest event on overflow — a rapid duplicate
 *   tap is discarded rather than the user's original intent.
 * - [BufferOverflow.DROP_OLDEST] would drop the first (intentional) action — wrong for
 *   navigation commands where the earliest event carries the user's actual intent.
 *
 * Buffer capacity of 1 is sufficient: the main-thread collector in [NavigationState]
 * processes events faster than a human can generate them. The buffer exists only as a
 * safety net for the brief window between app start and the LaunchedEffect collector
 * becoming active.
 */
@Stable // Singleton — navEvents SharedFlow reference never changes; safe to skip recomposition
@Singleton
class GlobalNavigator @Inject constructor() : NavigationBus {

    private val _navEvents = MutableSharedFlow<NavCommand>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val navEvents = _navEvents.asSharedFlow()

    override fun navigate(key: TravelNavKey) {
        _navEvents.tryEmit(NavCommand.Navigate(key))
    }

    override fun back() {
        _navEvents.tryEmit(NavCommand.Back)
    }
}
