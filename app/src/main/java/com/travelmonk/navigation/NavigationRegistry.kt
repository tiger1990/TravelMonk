package com.travelmonk.navigation

import androidx.compose.runtime.Stable
import com.travelmonk.core.common.config.AppConfig
import com.travelmonk.core.navigation.NavDestination
import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.core.navigation.TravelNavKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-layer registry that dispatches navigation keys to the correct handler.
 *
 * Handlers are contributed via Hilt multibindings (@Binds @IntoSet in NavHandlerModule).
 * Adding a new feature requires only:
 *  1. A new [NavKeyHandler] implementation in the feature/handler package
 *  2. One @Binds @IntoSet binding in NavHandlerModule
 *
 * NavigationRegistry and NavigationState need no changes.
 */
// Singleton — handler set Provided from NavHandlerModule is fixed after construction;
// safe to skip recomposition by annotating and marking this class stable
@Stable
@Singleton
class NavigationRegistry @Inject constructor(
    private val handlers: Set<@JvmSuppressWildcards NavKeyHandler>,
    private val appConfig: AppConfig
) {
    /**
     * Resolves a [TravelNavKey] to a [NavDestination] by finding the matching handler.
     * 
     * We use [AppConfig.isDebug] to perform an integrity check to ensure that no two 
     * handlers claim ownership of the same key. This prevents non-deterministic 
     * navigation behavior.
     */
    fun resolve(key: TravelNavKey): NavDestination? {
        if (appConfig.isDebug) {
            val matchingHandlers = handlers.filter { it.canHandle(key) }
            if (matchingHandlers.size > 1) {
                val handlerNames = matchingHandlers.joinToString { it::class.java.simpleName }
                throw IllegalStateException(
                    "Duplicate NavKeyHandlers found for key: ${key::class.java.simpleName}. " +
                    "Handlers: [$handlerNames]. Each key must be handled by exactly one feature."
                )
            }
        }
        
        return handlers.firstOrNull { it.canHandle(key) }?.resolve(key)
    }
}
