# TravelMonk — Architecture Gap Tracker

Sourced from the full architectural audit. Work through these in priority order.
Status legend: `[ ]` = todo · `[~]` = in progress · `[x]` = done

---

## CRITICAL

### GAP-01 — Process Death: NavigationState Not Persisted
- **Status:** `[x]`
- **File(s):** `app/src/main/java/com/travelmonk/ui/navigation/NavigationState.kt:87`
- **Problem:** `rememberNavigationState()` uses `remember { NavigationState(registry) }` — not `rememberSaveable`. All 6 per-tab back stacks (`mutableStateListOf`) and `currentTab` (`mutableStateOf`) are wiped on process death or low-memory kill. User returns to app at Home tab, screen 1.
- **Fix:**
  - Move `NavigationState` into an `ActivityViewModel` (or `HiltViewModel` scoped to the activity) so it survives configuration changes natively.
  - Persist the active tab + each tab's back stack using `SavedStateHandle`. Nav keys are `@Serializable` — encode as JSON strings or use `Bundle`-compatible serialization.
  - Replace `rememberNavigationState(registry)` with `hiltViewModel<NavigationViewModel>()` in `TravelMonkApp`.

---

### GAP-02 — BookingType Enum Mismatch → Runtime Crash
- **Status:** `[x]`
- **File(s):**
  - `feature/bookings/src/main/java/com/travelmonk/feature/bookings/mvi/BookingMvi.kt:21`
  - `feature/bookings/src/main/java/com/travelmonk/feature/bookings/ui/BookingViewModel.kt:47`
  - `core/model/src/main/java/com/travelmonk/core/model/Booking.kt`
- **Problem:** `BookingMvi.kt` redeclares a local `BookingType` enum with 6 variants (no `EXPERIENCE_PACKAGE`). `BookingViewModel` maps domain `BookingType` via `BookingType.valueOf(it.type.name)` — throws `IllegalArgumentException` at runtime when any booking with type `EXPERIENCE_PACKAGE` appears (triggered by booking from the Experiences screen).
- **Fix:**
  - Delete the local `enum class BookingType` from `BookingMvi.kt`.
  - Change `BookingItem.type` to `com.travelmonk.core.model.BookingType`.
  - Remove the `valueOf` mapping in `BookingViewModel` — pass `it.type` directly.

---

### GAP-03 — Silent Exception Swallowing / No Error State
- **Status:** `[ ]`
- **File(s):**
  - `feature/stays/src/main/java/com/travelmonk/feature/stays/ui/StayViewModel.kt:28`
  - `feature/flights/src/main/java/com/travelmonk/feature/flights/ui/FlightViewModel.kt:32`
  - `feature/experiences/src/main/java/com/travelmonk/feature/experiences/ui/ExperienceViewModel.kt:52`
  - `feature/bookings/src/main/java/com/travelmonk/feature/bookings/ui/BookingViewModel.kt:55`
- **Problem:** All four ViewModels catch exceptions with `// Handle error` (empty body). Errors are invisible — the screen appears frozen or empty with no user feedback. `StaySearchState`, `FlightSearchState`, and `ExperienceState` have no `error` field.
- **Fix:**
  - Add `error: String? = null` (or a sealed `UiError` type) to each `UiState`.
  - In each catch block: `setState { copy(isLoading = false, error = e.message) }`.
  - Display error UI in corresponding content composables.
  - Also: `FlightViewModel` assigns `results` from `searchFlights()` but never uses it — fix or remove.

---

## HIGH

### GAP-04 — No SavedStateHandle in Any ViewModel
- **Status:** `[ ]`
- **File(s):** All ViewModels:
  - `feature/flights/.../FlightViewModel.kt`
  - `feature/stays/.../StayViewModel.kt`
  - `feature/experiences/.../ExperienceViewModel.kt`
  - `feature/bookings/.../BookingViewModel.kt`
  - `feature/home/.../HomeViewModel.kt`
  - `feature/transport/.../TransportViewModel.kt`
  - `feature/services/.../ServicesViewModel.kt`
- **Problem:** No ViewModel uses `SavedStateHandle`. On process death + recreation, every ViewModel resets to `createInitialState()`. All user-entered data (flight search cities, stay location, selected filters) is lost. `FlightSearchState` defaults to hardcoded `"San Francisco"` / `"SFO"` — these overwrite user input.
- **Fix:**
  - Inject `SavedStateHandle` into each ViewModel.
  - Persist all user-mutated fields on every `setState` call (or use `SavedStateHandle.getStateFlow`).
  - `createInitialState()` should read from `savedStateHandle` with fallback defaults.

---

### GAP-05 — Missing Domain Use Cases (Clean Architecture Violation)
- **Status:** `[ ]`
- **File(s):** All feature `domain/` packages — none contain `UseCase` classes.
- **Problem:** ViewModels call repositories directly, violating Clean Architecture's domain layer contract. Business logic (filtering, transformation, orchestration) lives in ViewModels, making it untestable in isolation and non-reusable. A second screen needing the same operation will duplicate the call.
- **Fix:**
  - Add `UseCase` classes in each feature's `domain/` package (e.g., `GetBookingsUseCase`, `SearchFlightsUseCase`, `GetExperiencesUseCase`).
  - Each use case is a single-responsibility `operator fun invoke()` that wraps repository calls.
  - ViewModels inject and call use cases, not repositories directly.

---

### GAP-06 — `collectAsState()` Instead of `collectAsStateWithLifecycle()`
- **Status:** `[ ]`
- **File(s):**
  - `feature/bookings/.../MyBookingsScreen.kt:32`
  - `feature/experiences/.../ExperiencesScreen.kt:32`
  - `feature/home/.../HomeScreen.kt:36`
  - `feature/stays/.../StaySearchScreen.kt` (verify)
  - `feature/flights/.../FlightSearchScreen.kt:35`
- **Problem:** `collectAsState()` does not respect the Android lifecycle — Flow collection continues when the app is in the background (screen off, another app in foreground). Wastes CPU, battery, and network.
- **Fix:**
  - Add dependency: `androidx.lifecycle:lifecycle-runtime-compose`.
  - Replace every `viewModel.uiState.collectAsState()` with `viewModel.uiState.collectAsStateWithLifecycle()`.

---

### GAP-07 — `NavigationState` Imports All Feature API Nav Keys (Direct Coupling)
- **Status:** `[ ]`
- **File(s):** `app/src/main/java/com/travelmonk/ui/navigation/NavigationState.kt:13–18`
- **Problem:** `NavigationState` explicitly imports all 6 feature API nav keys to initialize each tab's starting route. Adding a new tab requires modifying this app-layer class. This partially defeats the feature-registry plug-in pattern already established by `NavEntryInstaller` and `NavKeyHandler`.
- **Fix:**
  - Define each tab's initial route inside `BottomBarItem` (it already holds the `route: TravelNavKey`).
  - Initialize each tab stack from `BottomBarItem.values()` by reading `.route` — `NavigationState` then has no feature-specific imports.
  - New tabs only require a new `BottomBarItem` entry and the existing registry hooks.

---

### GAP-08 — Feature Navigators Import Other Feature Nav Keys (Cross-Feature Coupling)
- **Status:** `[ ]`
- **File(s):**
  - `feature/home/src/main/java/com/travelmonk/feature/home/di/NavigatorModule.kt:6` — imports `TransportNavKey`
  - `feature/experiences/src/main/java/com/travelmonk/feature/experiences/di/NavigatorModule.kt:6` — imports `BookingNavKey`
  - `feature/flights/src/main/java/com/travelmonk/feature/flights/di/NavigatorModule.kt:6` — imports `BookingNavKey`
  - `feature/stays/src/main/java/com/travelmonk/feature/stays/di/NavigatorModule.kt` — verify
- **Problem:** Feature implementation modules directly import other features' nav keys, creating compile-time feature-to-feature dependencies. This breaks module isolation — a change to `BookingNavKey` forces recompilation of `experiences` and `flights`.
- **Fix (two options):**
  - **Option A:** Move shared cross-feature destinations (e.g., booking confirmation) to `core/navigation` as a shared nav key. Each feature navigates to the shared key.
  - **Option B:** Express cross-feature navigation as a higher-level action in `NavigationBus` (e.g., `bus.navigateToBookingConfirmation(type, title)`) defined in `core/navigation` — bus implementation in the app layer resolves it to the correct key without the feature knowing.

---

### GAP-09 — `FlightRepositoryImpl` Missing `@Singleton`
- **Status:** `[ ]`
- **File(s):** `feature/flights/src/main/java/com/travelmonk/feature/flights/data/repository/FlightRepositoryImpl.kt:8`
- **Problem:** `BookingRepositoryImpl` is `@Singleton`. `FlightRepositoryImpl` has no scope annotation — Hilt creates a new instance on every injection point. Any in-memory cache or shared state in the repository is lost between injections.
- **Fix:** Add `@Singleton` annotation to `FlightRepositoryImpl`. Audit `StayRepositoryImpl`, `ExperienceRepositoryImpl`, `ServiceRepositoryImpl` for the same issue.

---

### GAP-10 — Backwards-Compatibility Typealias Shim in Transport Module
- **Status:** `[ ]`
- **File(s):**
  - `feature/transport/src/main/java/com/travelmonk/feature/transport/api/TransportTabContentProvider.kt`
  - `feature/transport/src/main/java/com/travelmonk/feature/transport/mvi/TransportMvi.kt:7`
- **Problem:** The transport implementation module contains a shim file with `typealias TransportTabContentProvider = ...` and `typealias TransportTab = ...` pointing to the API module. `TransportMvi.kt` imports `com.travelmonk.feature.transport.api.TransportTab` (the alias) instead of the real type from `feature/transport-api`. This is a dead-weight indirection that causes confusion.
- **Fix:**
  - Delete `feature/transport/src/main/java/.../transport/api/TransportTabContentProvider.kt`.
  - Update `TransportMvi.kt` import to `com.travelmonk.feature.transportapi.TransportTab`.
  - Fix any other files in the transport module using the old alias path.

---

## MEDIUM

### GAP-11 — Hardcoded Colors Violate Design System Rules
- **Status:** `[ ]`
- **File(s):**
  - `feature/bookings/.../MyBookingsScreen.kt` — `Color(0xFF4CAF50)`, `Color(0xFFFFA000)`
  - `feature/experiences/.../ExperiencesScreen.kt` — `Color(0xFFFFC107)`
  - `feature/flights/.../FlightSearchScreen.kt` — `Color(0xFFF1F4F8)`
  - `feature/home/.../HomeScreen.kt` — `Color.White.copy(alpha = 0.8f)`, `Color.Black.copy(alpha = 0.3f)`
  - `feature/flights/.../FlightSearchScreen.kt:TripTypeSelector` — `Color(0xFFF1F4F8)`
- **Problem:** Raw color literals scattered across UI code. Violates `compose-ui-best-practice.md`: "Don't hardcode colors/typography." Dark mode, theming, and brand updates require hunting down every literal.
- **Fix:**
  - Add semantic color tokens to `TravelMonkColors` in `core/designsystem` (e.g., `statusSuccess`, `statusWarning`, `starRating`, `surfaceSubtle`).
  - Replace every `Color(0x...)` literal with `TravelMonkTheme.colors.<token>`.

---

### GAP-12 — HTTP Logging Always at `BODY` Level (Security Issue)
- **Status:** `[ ]`
- **File(s):** `core/network/src/main/java/com/travelmonk/core/network/di/NetworkModule.kt:31`
- **Problem:** `HttpLoggingInterceptor.Level.BODY` is unconditional. In release builds, full HTTP bodies — including auth tokens, session cookies, and PII — are written to Logcat, accessible to any app with `READ_LOGS` permission.
- **Fix:**
  ```kotlin
  fun provideLoggingInterceptor(appConfig: AppConfig): HttpLoggingInterceptor {
      return HttpLoggingInterceptor().apply {
          level = if (appConfig.isDebug) Level.BODY else Level.NONE
      }
  }
  ```
  Inject `AppConfig` into `NetworkModule` and gate log level behind `isDebug`.

---

### GAP-13 — Repositories Return `List<T>` Instead of `Flow<List<T>>`
- **Status:** `[ ]`
- **File(s):**
  - `feature/bookings/.../BookingRepository.kt` — `suspend fun getBookings(): List<Booking>`
  - `feature/home/.../HomeRepository.kt` — `suspend fun getHomeBanners(): List<HomeBanner>`
  - `feature/experiences/.../ExperienceRepository.kt` — `suspend fun getExperiences(...): List<Experience>`
  - All other feature repository interfaces
- **Problem:** No reactive pipeline. A booking cancellation in `BookingViewModel` manually re-fetches the full list. Changes made in one feature cannot propagate to another screen without a full reload trigger. Impossible to plug in local database cache (Room) with real-time updates.
- **Fix:**
  - Change repository return types to `Flow<List<T>>`.
  - ViewModels collect the flow and update state reactively.
  - Enables future Room integration (`.flowOn(Dispatchers.IO)` from DAO) without ViewModel changes.

---

### GAP-14 — No Deep Link Handling
- **Status:** `[ ]`
- **File(s):** `app/src/main/java/com/travelmonk/MainActivity.kt`
- **Problem:** `MainActivity.onCreate` does not process `intent`. No deep link routing, no notification tap handling, no external URL dispatch. For a travel app, booking confirmations, push notifications ("Your flight is tomorrow"), and marketing links all require deep links.
- **Fix:**
  - Add `intent-filter` entries in `AndroidManifest.xml` for app scheme / HTTPS deep links.
  - Handle `intent` in `MainActivity.onCreate` and `onNewIntent`.
  - Route the parsed path/params to the `NavigationBus` as the appropriate `TravelNavKey`.
  - Consider defining a `DeepLinkHandler` in `core/navigation` to keep `MainActivity` thin.

---

### GAP-15 — Effect Collection Race: `LaunchedEffect(Unit)` + `Channel`
- **Status:** `[ ]`
- **File(s):**
  - `feature/experiences/.../ExperiencesScreen.kt:34`
  - `feature/flights/.../FlightSearchScreen.kt:37`
  - `feature/home/.../HomeScreen.kt:38`
- **Problem:** `LaunchedEffect(Unit)` collects `viewModel.effect` (a `Channel`-backed flow). If the screen is recomposed or briefly removed from composition and re-added, the `LaunchedEffect` cancels and restarts — potentially missing an effect that was emitted in the gap. The `Channel.BUFFERED` capacity (64 by default) mitigates but does not eliminate this.
- **Fix (choose one):**
  - **Option A:** Use `repeatOnLifecycle(Lifecycle.State.STARTED)` via `LaunchedEffect` with the lifecycle as key, ensuring the collector restarts on every start.
  - **Option B:** Replace the `Channel` in `BaseViewModel` with a `MutableSharedFlow(replay = 1, extraBufferCapacity = 8)` for one-shot effects that must not be lost. Clear replay after consumption.
  - At minimum, document the chosen strategy as a convention in `BaseViewModel.kt`.

---

### GAP-16 — SharedFlow Buffer Overflow Strategy Under Navigation Load
- **Status:** `[ ]`
- **File(s):** `app/src/main/java/com/travelmonk/navigation/GlobalNavigator.kt:26`
- **Problem:** `MutableSharedFlow(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)`. If 9+ navigation events fire before the `LaunchedEffect` collector in `TravelMonkApp` resumes (e.g., on app cold start or after a brief background pause), the oldest events are silently dropped. `DROP_OLDEST` is counterintuitive for navigation — the most meaningful event is usually the earliest (the user's tap), not the latest.
- **Fix:**
  - Change to `onBufferOverflow = BufferOverflow.DROP_LATEST` to preserve earlier user-initiated commands, OR
  - Use `BufferOverflow.SUSPEND` (with a larger capacity) to ensure no event is ever dropped — this requires the emitter to be in a coroutine scope.
  - Ensure `TravelMonkApp`'s `LaunchedEffect` starts collecting before any navigation event can be emitted (e.g., gate feature initialization behind composition being ready).

---

## Progress Summary

| Gap | Severity | Status | Area |
|-----|----------|--------|------|
| GAP-01 | CRITICAL | `[x]` | Process Death — NavigationState |
| GAP-02 | CRITICAL | `[x]` | Crash — BookingType Mismatch |
| GAP-03 | CRITICAL | `[ ]` | Silent Error Swallowing |
| GAP-04 | HIGH | `[ ]` | Process Death — SavedStateHandle |
| GAP-05 | HIGH | `[ ]` | Clean Architecture — No Use Cases |
| GAP-06 | HIGH | `[ ]` | Lifecycle — collectAsState |
| GAP-07 | HIGH | `[ ]` | Module Coupling — NavigationState |
| GAP-08 | HIGH | `[ ]` | Module Coupling — Cross-Feature Nav Keys |
| GAP-09 | HIGH | `[ ]` | DI — FlightRepositoryImpl Singleton |
| GAP-10 | HIGH | `[ ]` | Module Hygiene — Typealias Shim |
| GAP-11 | MEDIUM | `[ ]` | Design System — Hardcoded Colors |
| GAP-12 | MEDIUM | `[ ]` | Security — HTTP Logging in Release |
| GAP-13 | MEDIUM | `[ ]` | Reactivity — List vs Flow |
| GAP-14 | MEDIUM | `[ ]` | Missing Feature — Deep Links |
| GAP-15 | MEDIUM | `[ ]` | Effect Delivery — LaunchedEffect Race |
| GAP-16 | LOW | `[ ]` | Navigation — SharedFlow Overflow Strategy |
