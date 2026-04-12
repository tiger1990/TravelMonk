# TravelMonk — Claude Code Guidelines

## Project Overview
TravelMonk is a modular Android travel booking app built with Kotlin, Jetpack Compose, and MVI architecture.

## Module Structure
```
app/                        # Entry point, navigation wiring, Hilt app component
core/
  common/                   # MVI base classes (BaseViewModel<S,I,E>), coroutine utils
  designsystem/             # Theme, color/typography/spacing/shape tokens
  model/                    # Domain data models (Booking, Location)
  navigation/               # TravelNavKey, NavKeyHandler, NavigationBus, NavDestination, NavTab
  network/                  # Retrofit + Moshi + OkHttp setup
  tokens/                   # TravelMonkIcons
  ui/                       # Shared UI components (WIP)
  database/                 # Room (WIP)
feature/
  <name>/                   # Feature implementation (UI, ViewModel, DI, navigation handler)
  <name>-api/               # Feature contract (navigator interface, nav keys)
```
##Workflow
You are a Principle Architect and expert in Android and Clean Architecture, Design Pattern and Kotlin.
1. First, think through the problem. Read the codebase and write a plan in tasks/todo.md
2. The plan should be checklist of todo items.
3. Check in with me before starting work I will verify the plan.
4. Then complete the todos one by one, marking them off as you go.
5. At every step, give me high-level explanation of what. You have changed.
6. Keep every change simple and minimal. Avoid big rewrites.
7. At the end, add a review section in todo.md and summarize the changes.


## Architecture Principles

### MVI Pattern
- All screens use `BaseViewModel<State, Intent, Effect>` from `core/common`
- State is exposed via `StateFlow`, side effects via `Channel` (exactly-once delivery)
- No business logic in composables — all intent handling in ViewModel
- Collect state with `collectAsStateWithLifecycle()`

### Navigation
Five-layer architecture:
1. ViewModel calls a typed navigator interface (e.g. `FlightNavigator`)
2. Navigator delegates to `NavigationBus`
3. `GlobalNavigator` emits `NavCommand` to `SharedFlow`
4. `TravelMonkApp` collects and calls `NavigationRegistry.resolve(key)`
5. Registry dispatches to `NavKeyHandler` multibindings → `NavDestination` → `NavigationState` updates per-tab stack

**Adding a new screen:**
- Define nav key in `feature/<name>-api` module
- Implement `NavKeyHandler` in `feature/<name>` module
- Bind via `@Binds @IntoSet` in the feature's `NavHandlerModule`
- No changes needed in app-layer navigation code

### Compose UI Rules
- Stateless composables by default — hoist state to the lowest common parent
- Never hardcode colors, typography, or spacing — use design system tokens from `TravelMonkTheme`
- Use `remember` for expensive work, `derivedStateOf` for computed state
- Avoid passing unstable/mutable objects to composables
- All composables must be previewable — extract stateless `Content` composables
- Follow Compose UDF(Unidirectional data flow) and stateless composable principles
- Use `@Stable` or `@Immutable` on data passed to composables when needed
- Every composable should have Preview for light and dark mode

### Dependency Injection
- Hilt throughout — convention plugins auto-apply Hilt to all feature modules
- Feature navigators bound in `app/di/NavigationModule.kt`
- `NavKeyHandler` implementations bound via `@IntoSet` in each feature's DI module

## Code Conventions
- Package structure: `com.travelmonk.<module>.<layer>` (e.g. `com.travelmonk.feature.flights.ui`)
- MVI files named `<Feature>Mvi.kt` containing `State`, `Intent`, `Effect` sealed classes
- Navigator interfaces live in `feature/<name>-api`, implementations wired in app module
- Use `kotlinx.serialization` for all data classes that cross nav/network boundaries

## Known Gaps (Do Not Regress)
Documented in `ArchitectureGaps.md`. Key rules:
- **Never** leave `HttpLoggingInterceptor` enabled in release builds
- **Never** skip error handling — all repository calls must handle failure states
- **Always** use `Dispatchers.IO` for network/disk operations in repositories
- **No** hardcoded strings — use string resources
- **No** mutable state casts (e.g. `as SnapshotStateList`) — use proper state holders

## Build
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew :<module>:compileDebugKotlin  # Compile a single module
```
