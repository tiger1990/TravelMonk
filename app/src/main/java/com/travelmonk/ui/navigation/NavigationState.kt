package com.travelmonk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import com.travelmonk.core.navigation.NavTab
import com.travelmonk.core.navigation.TravelNavKey
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey
import com.travelmonk.feature.homeapi.navigation.HomeNavKey
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.transportapi.navigation.TransportNavKey
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.navigation.NavCommand
import com.travelmonk.navigation.NavigationRegistry

/**
 * Manages per-tab back stacks and the active tab for the app's bottom navigation.
 *
 * State survival:
 * - [backStacks] use [rememberNavBackStack] — backed by rememberSaveable internally,
 *   survives both configuration changes and process death.
 * - [topLevelRoute] uses [rememberSerializable] — persists via onSaveInstanceState,
 *   survives both configuration changes and process death.
 *
 * Follows the Google Navigation3 multiple-back-stacks recipe:
 * https://developer.android.com/guide/navigation/navigation-3/recipes/multiple-backstacks
 */
class NavigationState(
    private val startRoute: TravelNavKey,
    private val registry: NavigationRegistry,
    // MutableState<NavKey> (base type) — NavKeySerializer works with NavKey.
    // All values written to this state are TravelNavKey instances; cast in getter is safe.
    private val topLevelRoute: MutableState<NavKey>,
    private val backStacks: Map<TravelNavKey, NavBackStack<NavKey>>
) {
    // The root key of the currently selected tab
    var currentRouteKey: TravelNavKey
        get() = topLevelRoute.value as TravelNavKey
        private set(value) { topLevelRoute.value = value }

    // Mapped BottomBarItem for UI selection state — derived from currentRouteKey
    val currentTab: BottomBarItem get() = currentRouteKey.toBottomBarItem()

    // Current tab's back stack — used by TravelMonkApp for bottom bar visibility check
    val activeBackStack: NavBackStack<NavKey>
        get() = backStacks[currentRouteKey] ?: backStacks.getValue(startRoute)

    fun selectTab(item: BottomBarItem) {
        currentRouteKey = item.route
    }

    /**
     * Navigates to [key]:
     *  1. Resolves which tab owns the key (via registry)
     *  2. Switches to that tab's root if different from the current one
     *  3. Pushes [key] onto that tab's back stack (deduplicated at top)
     */
    fun navigateTo(key: TravelNavKey) {
        val destination = registry.resolve(key)
        if (destination != null) {
            currentRouteKey = destination.navTab.toRootKey()
        }
        val stack = backStacks[currentRouteKey] ?: return
        if (stack.lastOrNull() != key) stack.add(key)
    }

    fun pop() {
        val stack = backStacks[currentRouteKey] ?: return
        if (stack.size > 1) stack.removeLastOrNull()
    }

    /**
     * Returns Navigation3 decorated entries for all currently active tab stacks.
     *
     * Optimization: We create and remember the decorators once for all stacks. Passing
     * a stable, remembered list to [rememberDecoratedNavEntries] prevents unnecessary
     * re-decoration churn during tab switches and within-tab navigation.
     * rememberViewModelStoreNavEntryDecorator : This scopes ViewModels to
     * individual screens (NavEntries), each destination gets its own ViewModelStore
     * `ie: Each screen gets its own unique instance of a ViewModel.
     *
     * rememberSaveableStateHolderNavEntryDecorator: This enables Saveable State for each screen
     * survive process death by saving state into the OS Bundle
     * `ie: Each screen gets its own unique instance of a SaveableStateHolder.
     *
     * IMPORTANT: We map all stacks to decorated entries outside the flatMap to preserve
     * identity and avoid re-calculating decorations in a loop, which causes lag.
     *
     * "Exit through home" pattern: home stack is always kept active underneath the
     * selected tab, matching the Google recipe's behavior.
     */
    @Composable
    fun toDecoratedEntries(
        entryProvider: (TravelNavKey) -> NavEntry<TravelNavKey>
    ): List<NavEntry<TravelNavKey>> {
        val saveableStateDecorator = rememberSaveableStateHolderNavEntryDecorator<TravelNavKey>()
        val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<TravelNavKey>()
        val decorators = remember(saveableStateDecorator, viewModelStoreDecorator) {
            listOf(saveableStateDecorator, viewModelStoreDecorator)
        }

        @Suppress("UNCHECKED_CAST")
        val decoratedByRoute = backStacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack as NavBackStack<TravelNavKey>,
                entryDecorators = decorators,
                entryProvider = entryProvider
            )
        }
        
        return activeRoutes().flatMap { decoratedByRoute[it] ?: emptyList() }
    }

    // Home stack is always included so its state is maintained while on other tabs
    private fun activeRoutes(): List<TravelNavKey> =
        if (currentRouteKey == startRoute) listOf(startRoute)
        else listOf(startRoute, currentRouteKey)

    private fun NavTab.toRootKey(): TravelNavKey = when (this) {
        NavTab.HOME        -> HomeNavKey.Root
        NavTab.TRANSPORT   -> TransportNavKey.Root
        NavTab.STAYS       -> StayNavKey.Search
        NavTab.EXPERIENCES -> ExperienceNavKey.Root
        NavTab.SERVICES    -> ServiceNavKey.Root
        NavTab.BOOKINGS    -> BookingNavKey.Root
    }

    // Maps a tab root NavKey back to a BottomBarItem for UI rendering.
    // The else branch covers deep-linked sub-screens (e.g. FlightNavKey.Results as currentRouteKey
    // would be unusual but safe to fall back to Home).
    private fun TravelNavKey.toBottomBarItem(): BottomBarItem = when (this) {
        HomeNavKey.Root       -> BottomBarItem.Home
        TransportNavKey.Root  -> BottomBarItem.Transport
        StayNavKey.Search     -> BottomBarItem.Stays
        ExperienceNavKey.Root -> BottomBarItem.Experiences
        ServiceNavKey.Root    -> BottomBarItem.Services
        BookingNavKey.Root    -> BottomBarItem.Bookings
        else                  -> BottomBarItem.Home
    }
}

/**
 * Creates and remembers a [NavigationState] whose back stacks and active tab survive
 * configuration changes and process death via Navigation3's built-in APIs.
 *
 * The [GlobalNavigator] event stream is collected here rather than in TravelMonkApp —
 * "when you create navigation state, also wire up the event stream" is the right semantic.
 * This keeps TravelMonkApp free of plumbing while preserving the correct Compose scope
 * (LaunchedEffect lives in the composition, not the Singleton).
 */
@Composable
fun rememberNavigationState(
    registry: NavigationRegistry,
    globalNavigator: GlobalNavigator
): NavigationState {
    val startRoute: TravelNavKey = HomeNavKey.Root

    // rememberSerializable persists the active tab root key through process death.
    // NavKeySerializer handles polymorphic TravelNavKey subtypes via their @Serializable
    // + @SerialName annotations — no manual SerializersModule needed.
    val topLevelRoute: MutableState<NavKey> = rememberSerializable(
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startRoute)
    }

    // rememberNavBackStack wraps each stack in rememberSaveable — survives process death.
    val backStacks: Map<TravelNavKey, NavBackStack<NavKey>> = mapOf(
        HomeNavKey.Root       to rememberNavBackStack(HomeNavKey.Root),
        TransportNavKey.Root  to rememberNavBackStack(TransportNavKey.Root),
        StayNavKey.Search     to rememberNavBackStack(StayNavKey.Search),
        ExperienceNavKey.Root to rememberNavBackStack(ExperienceNavKey.Root),
        ServiceNavKey.Root    to rememberNavBackStack(ServiceNavKey.Root),
        BookingNavKey.Root    to rememberNavBackStack(BookingNavKey.Root),
    )

    val state = remember(registry) {
        NavigationState(
            startRoute    = startRoute,
            registry      = registry,
            topLevelRoute = topLevelRoute,
            backStacks    = backStacks
        )
    }

    // Bridge: Singleton event stream → Compose state.
    // LaunchedEffect stays in the composition (correct scope), GlobalNavigator stays pure Kotlin.
    LaunchedEffect(state, globalNavigator) {
        globalNavigator.navEvents.collect { command ->
            when (command) {
                is NavCommand.Navigate -> state.navigateTo(command.key)
                is NavCommand.Back     -> state.pop()
            }
        }
    }

    return state
}
