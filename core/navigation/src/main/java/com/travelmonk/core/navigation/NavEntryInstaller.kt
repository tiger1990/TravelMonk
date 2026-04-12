package com.travelmonk.core.navigation

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.EntryProviderScope

/**
 * Contract each feature team implements to contribute composable screen entries
 * to the app-level navigation graph.
 *
 * Declared as a member extension so feature modules can use the clean DSL syntax:
 *
 *     NavEntryInstaller = {
 *         entry<MyNavKey.Root> { MyScreen() }
 *     }
 *
 * Features register installers via Hilt multibindings (@Provides @IntoSet) in
 * ActivityRetainedComponent. The app layer collects all installers and invokes
 * them — no feature-specific imports needed in the app.
 */
fun interface NavEntryInstaller {
    fun EntryProviderScope<TravelNavKey>.install()
}

/**
 * Stable wrapper around the Hilt-injected [Set] of [NavEntryInstaller]s.
 *
 * [Set] is an interface, so the Compose compiler treats it as unstable and
 * prevents [TravelMonkApp] from ever being skipped during recomposition.
 * Wrapping it in an [@Immutable][Immutable] value class signals to the compiler
 * that the contents never change after injection — which is guaranteed by Hilt's
 * [ActivityRetainedComponent] scope.
 */
@Immutable
@JvmInline
value class NavEntryInstallerSet(val installers: Set<NavEntryInstaller>)
