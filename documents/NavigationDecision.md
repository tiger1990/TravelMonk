Navigation Architecture Design Decisions and Implementation

Suggestion Given:
Approach1:
The Mental Model

  ViewModel  →  FeatureNavigator  →  NavigationBus  →
    GlobalNavigator (SharedFlow)
             ↓
   App observer collects event
             ↓
   NavigationState resolves key → tab stack

  Layer by layer, with your actual code

  Layer 1 — Feature ViewModel knows nothing about
  routing:
  // FlightSearchViewModel.kt
  class FlightSearchViewModel @Inject constructor(
      private val navigator: FlightNavigator
  ) : ViewModel() {
      fun onSearchClicked() {

  navigator.navigateTo(FlightNavKey.Results(from =
  "DEL", to = "BOM"))
      }
  }

  Layer 2 — FeatureNavigator is a typed contract in
  the -api module. The ViewModel only knows this
  interface — no Hilt, no bus, no routing logic:
  // FlightNavigator.kt (feature/flights-api)
  interface FlightNavigator {
      fun navigateTo(key: FlightNavKey)
      fun back()
  }

  Layer 3 — NavigationModule provides a thin adapter.
   The feature never sees NavigationBus directly:
  // Your current NavigationModule.kt
  fun provideFlightNavigator(bus: NavigationBus):
  FlightNavigator = object : FlightNavigator {
      override fun navigateTo(key: FlightNavKey) =
  bus.navigate(key)
      override fun back() = bus.back()
  }

  Layer 4 — GlobalNavigator just emits the event onto
   a SharedFlow. It has zero routing knowledge:
  // Your current GlobalNavigator.kt
  override fun navigate(key: TravelNavKey) {
      _navEvents.tryEmit(NavCommand.Navigate(key))
  }

  Layer 5 — App observer collects the event and
  resolves which tab the key belongs to. This is the
  missing piece in your current NavigationState.

  Right now NavigationState.navigateTo() pushes to
  whichever tab is currently active. It needs to be
  smarter:

  // NavigationState.kt — smart routing
  fun navigateTo(key: TravelNavKey) {
      // 1. Resolve which tab owns this key
      val targetTab = resolveTab(key)

      // 2. Switch tab if needed
      if (targetTab != null && currentTab !=
  targetTab) {
          currentTab = targetTab
      }

      // 3. Push onto that tab's stack
      val stack = currentMutableStack()
      if (stack.lastOrNull() != key) stack.add(key)
  }

  private fun resolveTab(key: TravelNavKey):
  BottomBarItem? = when (key) {
      is HomeNavKey       -> BottomBarItem.Home
      is TransportNavKey  -> BottomBarItem.Transport
      is FlightNavKey     -> BottomBarItem.Transport
   // Flights live under Transport tab
      is StayNavKey       -> BottomBarItem.Stays
      is ExperienceNavKey ->
  BottomBarItem.Experiences
      is ServiceNavKey    -> BottomBarItem.Services
      is BookingNavKey    -> BottomBarItem.Bookings
      else                -> null  // stay on current
   tab
  }

  ---
  Why this design wins

  Concern: "I want to go to Results screen"
  Lives where: ViewModel
  Knows about: Only FlightNavKey
  ────────────────────────────────────────
  Concern: "FlightNavigator maps to bus"
  Lives where: NavigationModule (app)
  Knows about: FlightNavKey + NavigationBus
  ────────────────────────────────────────
  Concern: "Emit the event"
  Lives where: GlobalNavigator
  Knows about: Only TravelNavKey (sealed base)
  ────────────────────────────────────────
  Concern: "Which tab owns this key"
  Lives where: NavigationState.resolveTab()
  Knows about: All key types

  The only place that knows the key→tab mapping is
  resolveTab(). Adding a new feature means adding one
   when branch there and one @Provides in
  NavigationModule — nothing else changes.

Comparison For NavigationBus and NavigationRegistry
  ┌───────────────┬─────────────────┬─────────────┐
  │               │ NavigationBus + │  Registry   │
  │               │   resolveTab()  │   pattern   │
  ├───────────────┼─────────────────┼─────────────┤
  │ Routing logic │ One when in     │ Distributed │
  │  lives        │ NavigationState │  across     │
  │               │                 │ handlers    │
  ├───────────────┼─────────────────┼─────────────┤
  │               │                 │ Write a     │
  │ Adding a new  │ Add one when    │ handler     │
  │ feature       │ branch          │ class +     │
  │               │                 │ register it │
  ├───────────────┼─────────────────┼─────────────┤
  │ Cross-feature │ resolveTab()    │ Handler     │
  │  navigation   │ handles it      │ decides tab │
  │               │                 │  explicitly │
  ├───────────────┼─────────────────┼─────────────┤
  │               │                 │ Higher —    │
  │ Complexity    │ Low             │ more moving │
  │               │                 │  parts      │
  ├───────────────┼─────────────────┼─────────────┤
  │               │                 │ Yes, still  │
  │ App knows all │ Yes, explicitly │ — handlers  │
  │  keys?        │                 │ live in app │
  │               │                 │  layer      │
  └───────────────┴─────────────────┴─────────────┘

Suggestion-Accepted
Approach2:
Here's the complete picture of what was built
   and why it scales for multiple teams:

  ---
  What changed and why it scales

  core:navigation
    ├── NavKeyHandler.kt    ← interface any team
  implements (no Hilt, pure Kotlin)
    └── NavDestination.kt   ← data: key to push +
  which tab owns it

  app/navigation
    ├── NavigationRegistry  ← @Singleton, dispatches
  to the right handler
    └── handlers/
          ├── FlightNavKeyHandler   ← Flights →
  Transport tab
          ├── BookingNavKeyHandler  ← all
  BookingNavKey variants → Bookings tab
          └── ... (one per feature)

  app/di
    └── NavHandlerModule    ← @Binds @IntoSet per
  handler (the wiring point)

  Adding a new feature (e.g. Hotels) in future:
  1. New team creates HotelNavKeyHandler — no access
  to registry needed
  2. Adds one @Binds @IntoSet in NavHandlerModule in
  the app
  3. NavigationRegistry, NavigationState,
  TravelMonkApp — zero changes

  The @IntoSet multibinding is the key — Hilt
  assembles the Set<NavKeyHandler> from all bindings
  across all modules automatically. Each team owns
  exactly their handler, nothing else.


The registry and the   
existing NavigationModule serve different          
responsibilities — they didn't overlap.

Here's the full picture of why each piece still    
earns its place:
                                                     
---                                                
NavigationModule — still fully needed

┌───────────────────┬──────────────────────────┐
│   What it does    │     Why still needed     │   
├───────────────────┼──────────────────────────┤   
│ @Binds            │ GlobalNavigator is still │
│ NavigationBus →   │  the NavigationBus       │
│ GlobalNavigator   │ implementation; nothing  │   
│                   │ replaced that            │
├───────────────────┼──────────────────────────┤   
│ @Provides         │ TravelEntryProvider      │   
│ FlightNavigator   │ still passes typed       │   
│ etc.              │ navigators into screens  │
└───────────────────┴──────────────────────────┘
                                                     
---
The two systems solve different problems:

Screen triggers navigation:
FlightResultsScreen →                            
FlightNavigator.navigateTo(key)    ←               
NavigationModule's job
↓                    
NavigationBus.navigate(key)
↓                    
GlobalNavigator SharedFlow
emits                                              
↓                    
TravelMonkApp collects →
navigationState.navigateTo(key)                    
↓                  
Registry resolves key →     
which tab   ← NavHandlerModule's job               
↓
Switch tab + push key onto  
stack

- NavigationModule = outgoing boundary — screens   
  get a typed navigator so they can only navigate to
  keys they own (FlightNavigator can't accidentally  
  navigate to BookingNavKey)
- NavHandlerModule + Registry = incoming routing —
  resolves any arriving key to its owning tab

They are complementary layers, not competing ones.
Removing NavigationModule would force screens to   
accept NavigationBus directly, which breaks feature
isolation — a FlightResultsScreen could then      
navigate to any key in the entire app.

Nothing is redundant. Nothing needs to be removed.

For improving compile time optimation in TravelMonkApp 
added @Stable for Navigator and NavigationRegistry

@Stable not @Immutable — @Immutable
requires all public properties to be
deeply immutable (primitives or other
@Immutable types). SharedFlow,
Set<NavKeyHandler>, and interface
implementations can't guarantee that.
@Stable is the right contract: "if
two instances are equals(), Compose
can skip recomposing." For
singletons, this is always true.

On interfaces — annotating the
interface is a promise that all
implementations honour stability.
This is valid here because every
implementation is a Hilt @Singleton
anonymous object — their references
never change after injection.

-api modules already have Compose
dependency — via the
travelmonk.android.library.compose
convention plugin applied through the
feature convention, so
androidx.compose.runtime.Stable is
available without adding any new
dependency.

Iteration:
Plan: NavTab Enum + Move Handlers to Feature
Modules

     NavDestination.tabRootKey: TravelNavKey forces
     handlers to import foreign feature keys.
     FlightNavKeyHandler imports TransportNavKey.Root
      just to say "I belong to the Transport tab" —
     a cross-feature dependency that prevents moving
     handlers out of the app module.

     Replacing tabRootKey with a NavTab enum (in
     core:navigation, which every feature already
     depends on) eliminates the cross-feature import
     entirely. Handlers then move to their respective
     feature implementation modules (which already
     have Hilt via AndroidFeatureConventionPlugin),
     and each feature owns its own Hilt @IntoSet
     binding. NavHandlerModule in the app is deleted

┌──────────┬───────────────────────────────────┐
│ Who adds │                                   │
│  a new   │          What they touch          │
│ feature  │                                   │
├──────────┼───────────────────────────────────┤
│ Feature  │ Create XNavKeyHandler +           │
│ team     │ XNavHandlerModule in their        │
│          │ feature module                    │
├──────────┼───────────────────────────────────┤
│ Feature  │ Add @Binds @IntoSet — Hilt        │
│ team     │ aggregates it automatically       │
├──────────┼───────────────────────────────────┤
│ Platform │ Add entry to NavTab enum + one    │
│  team    │ when branch in                    │
│          │ NavigationState.toBottomBarItem() │
├──────────┼───────────────────────────────────┤
│          │ NavigationRegistry,               │
│ Nobody   │ NavHandlerModule (deleted),       │
│          │ app-layer handlers (deleted)      │
└──────────┴───────────────────────────────────┘

FlightNavKeyHandler now imports only FlightNavKey
and NavTab.TRANSPORT — zero cross-feature dependencies.
