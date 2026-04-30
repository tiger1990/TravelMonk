package com.travelmonk.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.travelmonk.core.logger.TravelMonkLogger

private const val TAG = "ScreenLifecycle"

/**
 * Logs screen entry and exit along with the [LocalViewModelStoreOwner] backing any
 * [hiltViewModel()] calls on this screen.
 *
 * Combined with the BaseViewModel init/onCleared logs (tag "ViewModel"), this lets you
 * correlate exactly which owner holds a ViewModel and how long each lives.
 *
 * Call at the top of every stateful *Screen composable.
 */
@Composable
fun LogScreenLifecycle(screenName: String) {
    val owner = LocalViewModelStoreOwner.current
    DisposableEffect(screenName) {
        val ownerClass = owner?.javaClass?.simpleName ?: "unknown"
        val ownerHash = owner?.let { Integer.toHexString(System.identityHashCode(it)) } ?: "?"
        TravelMonkLogger.d(
            tag = TAG,
            msg = "$screenName entered | vmOwner=$ownerClass@$ownerHash"
        )
        onDispose {
            TravelMonkLogger.d(
                tag = TAG,
                msg = "$screenName exited | vmOwner=$ownerClass@$ownerHash"
            )
        }
    }
}
