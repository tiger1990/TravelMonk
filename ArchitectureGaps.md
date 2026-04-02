# TravelMonk — Architecture Review & Gaps

> Reviewed by: Senior Principal Architect
> Date: 2026-04-02
> Status legend: `[ ]` open · `[~]` in progress · `[x]` fixed

---

## Overall Assessment

The foundation is solid. MVI, convention plugins, feature modularization, navigator abstraction, and the design system token system are all pointing in the right direction. However, there are concrete gaps across every layer that would block a production release at a company like Agoda or Stripe. Below is a full breakdown organized by area, followed by a prioritized fix tracker.

---

## What Is Working Well

| Area | What's Right |
|---|---|
| **MVI** | `BaseViewModel<S,I,E>` enforces unidirectional data flow. `Channel`-based effects guarantee exactly-once delivery — no replay on recomposition |
| **Convention plugins** | Single `travelmonk.android.feature` plugin wires Compose + Hilt + all core deps with zero boilerplate per feature |
| **Feature structure** | Every feature consistently owns `mvi/`, `ui/`, `domain/`, `data/`, `navigator/`, `di/` — predictable, scalable |
| **Navigator abstraction** | Features depend on navigator interfaces, not on the app module. `GlobalNavigator` binds at runtime via `DisposableEffect` |
| **Transport multibindings** | `@IntoSet` Hilt multibindings let flights/bus/train register tabs without modifying the transport module — genuine plugin architecture |
| **Stateful/Stateless split** | Every screen separates the hiltViewModel entry point from previewable stateless content |
| **Design system tokens** | `staticCompositionLocalOf` for immutable tokens, `CompositionLocalProvider` for dark/light-aware colors — correct use of Compose primitives |
| **Per-tab back stacks** | Each bottom-bar tab maintains isolated navigation history — standard mobile UX done right |
| **transport-api module** | Interface segregation between `feature:transport` (consumer) and `feature:flights` (provider) via a dedicated contract module |

---

## Section 1 — Navigation

### 1.1 Unsafe mutable cast in `NavigationState`

`NavigationState.kt:43` — `(backStack as MutableList<TravelNavKey>).add(key)` bypasses the immutability contract. If the Compose snapshot system observes the underlying list type changing, recomposition behavior is undefined.

```kotlin
// Current — fragile unsafe cast
fun navigateTo(key: TravelNavKey) {
    (backStack as MutableList<TravelNavKey>).add(key)
}

// Fix — each tab stack is already SnapshotStateList, return it directly
private fun currentMutableStack(): SnapshotStateList<TravelNavKey> = when (currentTab) {
    BottomBarItem.Home        -> homeStack
    BottomBarItem.Transport   -> transportStack
    ...
}
fun navigateTo(key: TravelNavKey) { currentMutableStack().add(key) }
```

### 1.2 `NavigationState` not saved on process death

`TravelMonkApp.kt` creates `NavigationState` via `remember { NavigationState() }`. This lives only in composition memory. If the OS kills the process, the entire back stack is lost. Users are dropped back to the Home tab root on resume — a crash-like UX.

**Fix:** Back `NavigationState` with a ViewModel and `SavedStateHandle`, or use `rememberSaveable` with a custom `Saver` that serializes the key lists.

### 1.3 `BookingConfirmationScreen` lives in the app module

`TravelEntryProvider.kt:72` owns `BookingConfirmationScreen` — feature-specific UI has leaked into the app wiring layer. The app module should only register routes, never own composables.

**Fix:** Move `BookingConfirmationScreen` to `feature:bookings`. App module entry becomes:
```kotlin
entry<BookingNavKey.Confirmation> { key ->
    BookingConfirmationScreen(type = key.type, title = key.title, onDone = { ... })
}
```

### 1.4 `GlobalNavigator` violates SRP — will become a god class

`GlobalNavigator.kt` already implements 5 navigator interfaces. Every new feature adds another `override fun navigateTo(key: XxxNavKey)`. A real travel app has 15+ features (car rental, visa, insurance, hotel upgrades, etc.) — this becomes a 300-line god class.

**Fix:** Introduce a `NavigationBus` or typed key registry. Features push any `TravelNavKey` to the bus. App-layer resolves it to the correct tab stack. GlobalNavigator is replaced entirely.

```kotlin
@Singleton
class NavigationBus @Inject constructor() {
    private var handler: ((TravelNavKey) -> Unit)? = null
    fun bind(handler: (TravelNavKey) -> Unit) { this.handler = handler }
    fun navigate(key: TravelNavKey) { handler?.invoke(key) }
}
```

### 1.5 No deeplink support

`entryProvider` has no URI routing. Push notifications, web redirects, and widget taps all require deeplinks. Retrofitting this later across a populated entry provider is expensive.

**Fix:** Define a URI scheme (`travelmonk://flights/results?from=DEL&to=BOM`) and map URIs to `TravelNavKey` instances. Wire into `entryProvider` and declare `<intent-filter>` in `AndroidManifest.xml`.

### 1.6 `onBook` callback in `FlightResultsScreen` calls `stayNavigator.back()`

`TravelEntryProvider.kt` — `onBook = { airline -> stayNavigator.back() }` — a flight booking action is calling the stay navigator's back. This is a placeholder that will silently do the wrong thing.

**Fix:** Wire `onBook` to the proper booking confirmation navigation before the flights feature is considered complete.

---

## Section 2 — Clean Architecture

### 2.1 No Use Case / Interactor layer

Every ViewModel calls repositories directly. This breaks down when:
- Multiple ViewModels need the same business logic
- You need to compose data from two repositories
- You want to unit test business logic independently of Android

```kotlin
// Current — ViewModel talks directly to repository
class FlightViewModel @Inject constructor(
    private val flightRepository: FlightRepository
)

// Production pattern — ViewModel talks to use case
class FlightViewModel @Inject constructor(
    private val searchFlightsUseCase: SearchFlightsUseCase
)

class SearchFlightsUseCase @Inject constructor(
    private val flightRepository: FlightRepository,
    private val userRepository: UserRepository  // composable — impossible without use case layer
)
```

### 2.2 Domain models defined inside MVI files

`ExperienceItem` is defined in `ExperienceMvi.kt`. `BookingItem` is defined in `BookingMvi.kt`. MVI files should contain only UI contracts (`State`, `Intent`, `Effect`). When the API shape changes, you should not be touching MVI files.

**Fix:** Move domain models to `feature/*/domain/model/`. MVI State references the domain model type.

### 2.3 Business logic / hardcoded data inside composables

`HomeScreen.kt:149` — the category list is hardcoded inside `CategorySection()`:

```kotlin
val categories = listOf(
    "Flights" to TravelMonkIcons.Flight,
    "Hotels" to TravelMonkIcons.Hotel,
    "Tours"   to TravelMonkIcons.Explore,
    "Yoga"    to TravelMonkIcons.SelfImprovement
)
```

This is business data masquerading as UI code. It should live in state, driven by a use case / repository, so it can be server-controlled or A/B tested.

### 2.4 Flight search results not stored in state

`FlightViewModel` calls `flightRepository.searchFlights(...)` but discards the results — it only emits `NavigateToResults(from, to)`. `FlightResultsScreen` then loads its own data independently. The search results are loaded twice and the loading state shown twice.

**Fix:** Store `List<Flight>` in `FlightSearchState` after the search call, pass it through the navigation key or a shared ViewModel scoped to the transport back stack.

---

## Section 3 — ViewModel & MVI

### 3.1 `collectAsState()` instead of `collectAsStateWithLifecycle()`

Every screen uses `collectAsState()`. This keeps the coroutine active even when the app is backgrounded, wasting resources and potentially triggering UI updates the user never sees.

**Fix:** Replace with `collectAsStateWithLifecycle()` from `lifecycle-runtime-compose` across all screens.

### 3.2 `LaunchedEffect(Unit)` for effect collection ignores lifecycle

`LaunchedEffect(Unit)` runs once and collects effects through backgrounding. The proper lifecycle-aware pattern:

```kotlin
// Current
LaunchedEffect(Unit) { viewModel.effect.collect { ... } }

// Fix
LaunchedEffect(viewModel.effect, lifecycleOwner) {
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.effect.collect { ... }
    }
}
```

### 3.3 Unnecessary coroutine wrapping for synchronous `setState`

`HomeViewModel.kt:24` — `viewModelScope.launch { setState { copy(isLoading = true) } }`. `setState` is a synchronous `MutableStateFlow.value` assignment. Wrapping it in a coroutine is misleading noise and creates a tiny unnecessary dispatch.

```kotlin
// Current
viewModelScope.launch { setState { copy(isLoading = true) } }

// Fix
setState { copy(isLoading = true) }
viewModelScope.launch {
    val banners = homeRepository.getHomeBanners()
    setState { copy(banners = banners, isLoading = false) }
}
```

### 3.4 `SwapCities` reads `currentState` inside intent handler

`FlightViewModel.kt:18`:
```kotlin
is FlightIntent.SwapCities -> setState {
    copy(fromCity = intent.to, fromCode = currentState.toCode, ...)
    //                                    ^^ should be intent.fromCode
}
```
Reading `currentState` inside an intent handler creates a race condition if two intents arrive concurrently. The intent should carry the full snapshot needed.

### 3.5 No `Dispatchers.IO` for repository calls

`viewModelScope.launch { repo.networkCall() }` defaults to `Dispatchers.Main`. Network and disk operations must be dispatched to IO.

**Fix:** Repositories internally wrap with `withContext(Dispatchers.IO)`. Callers (ViewModels) remain dispatcher-agnostic.

---

## Section 4 — Error Handling

### 4.1 Errors silently swallowed across the entire data layer

All repositories use `catch (e: Exception)` to return mock data. At scale this means a real API failure — network timeout, 503, auth expiry — looks identical to a successful response to the user.

`HomeViewModel` sets `error` in state but `HomeContent` never renders it. `FlightEffect.ShowError` has an empty handler body.

**Fix:** Introduce a `DataResult` wrapper. Repositories return it. ViewModels map to state. Screens render error UI:

```kotlin
sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val exception: Throwable) : DataResult<Nothing>()
    data object Loading : DataResult<Nothing>()
}
```

---

## Section 5 — Design System

### 5.1 `core:ui` is completely empty

The module is declared in `settings.gradle.kts` and has a `build.gradle.kts` with Compose deps, but contains zero files. Every feature independently reimplements cards, buttons, and input fields.

`RoundedCornerShape(16.dp)` for cards appears in Home, Flights, Stays, Experiences, and Services. A button with `height(56.dp)` and `RoundedCornerShape(16.dp)` appears in at least 4 screens.

**Fix:** Implement shared components in `core:ui`:
- `TravelMonkButton` — standard CTA button using theme tokens
- `TravelMonkCard` — standard card with consistent elevation and corner radius
- `TravelMonkTextField` — search/input field with design system styling

### 5.2 Design tokens bypassed throughout UI code

Despite having a complete token system, screens hardcode values extensively:

| File | Violation | Correct Token |
|---|---|---|
| `HomeScreen.kt:82` | `Modifier.size(28.dp)` | `TravelMonkTheme.dimensions.iconMedium` |
| `HomeScreen.kt:70` | `padding(horizontal = 24.dp, vertical = 32.dp)` | `TravelMonkTheme.spacing.large / .extraLarge` |
| `BannerSection` | `width(300.dp)`, `height(150.dp)` | `TravelMonkTheme.dimensions.*` |
| `FlightSearchScreen.kt:94` | `offset(y = (-30).dp)` | Named dimension token |
| `FlightSearchScreen.kt:133` | `fontSize = 18.sp, fontWeight = FontWeight.Bold` | `TravelMonkTheme.typography.titleLarge` |
| `FlightSearchScreen.kt:109` | `Color(0xFFF1F4F8)` | Named color token in `Color.kt` |
| Multiple screens | `RoundedCornerShape(16.dp)` | `TravelMonkTheme.radius.medium` |
| Multiple screens | `RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)` | `TravelMonkTheme.radius.extraLarge` |
| `BookingConfirmationScreen` | `Color(0xFF4CAF50)` | `SuccessGreen` from `Color.kt` |

### 5.3 `core:tokens` is an orphaned module

`TravelMonkIcons` lives in a standalone `core:tokens` Gradle module instead of inside `core:designsystem`. Features must depend on two separate design-related modules. Icons are a design system concern.

**Fix:** Move `TravelMonkIcons` to `core:designsystem/icons/` and remove the `core:tokens` module.

---

## Section 6 — Testing

### 6.1 Zero test coverage

No test files exist beyond the generated `ExampleInstrumentedTest` and `ExampleUnitTest` templates.

**Missing test infrastructure:**
- No fake/stub repository implementations
- No `MainDispatcherRule` for ViewModel coroutine tests
- No test fixtures or domain model builders
- No `BaseViewModel` tests for effect delivery ordering or state reduction
- No Compose UI tests

**Minimum required before beta:**

| Test | Type | Covers |
|---|---|---|
| `FlightViewModelTest` | Unit | State transitions, effect on search, swap city race condition |
| `HomeViewModelTest` | Unit | Initial load, error state, banner click effect |
| `ExperienceViewModelTest` | Unit | Category filter, item booking effect |
| `FlightRepositoryImplTest` | Unit | Mock API success, mock API failure → DataResult.Error |
| `NavigationStateTest` | Unit | Tab switching, back stack push/pop, process death restore |

---

## Section 7 — Security & Production Readiness

### 7.1 `HttpLoggingInterceptor` at `BODY` level in all builds

`NetworkModule.kt` logs full request/response bodies including auth tokens in release builds.

**Fix:**
```kotlin
val level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
HttpLoggingInterceptor().setLevel(level)
```

### 7.2 No R8 / ProGuard rules

No `proguard-rules.pro` entries for Moshi, Retrofit, or Hilt. Without keep rules, release builds strip annotated classes and crash at runtime.

**Fix:** Add keep rules for:
- `@JsonClass(generateAdapter = true)` annotated data classes
- Retrofit service interfaces
- Hilt component entry points

### 7.3 No build flavors for environment separation

Single build config. Base URL is hardcoded as `"https://api.travelmonk.com/"` in `NetworkModule`. No way to point debug builds at a staging server.

**Fix:** Add `debug` / `staging` / `release` product flavors. Move base URL and API keys to `BuildConfig` fields via `buildConfigField`.

### 7.4 No certificate pinning

Booking and payment API endpoints should use OkHttp `CertificatePinner` to prevent MITM attacks.

### 7.5 Navigation args carry sensitive data as plain strings

`BookingNavKey.Confirmation(type: String, title: String)` — booking details passed as plain navigation arguments. Consider whether more sensitive booking data (price, reference ID) needs to be passed through a shared ViewModel rather than navigation args.

---

## Section 8 — Accessibility & Localization

### 8.1 `contentDescription = null` throughout

Every icon in `HomeScreen`, `FlightSearchScreen`, `TransportUI`, etc. passes `contentDescription = null`. VoiceOver / TalkBack users get no information. Decorative icons should explicitly document the intent with a comment; interactive icons need real descriptions.

### 8.2 All user-visible strings hardcoded in Kotlin

`"Hello Traveler,"`, `"Search Flights"`, `"Where to next?"`, `"Refer & Earn"` — zero `strings.xml` usage across 7 features. Making this app available in Hindi, Spanish, or Arabic requires a full rewrite of every screen.

**Fix:** Move all user-visible strings to `res/values/strings.xml` in each feature module. Use `stringResource(R.string.*)` in composables.

---

## Section 9 — Observability

### 9.1 No crash reporting

No Firebase Crashlytics or Sentry integration. In production, crashes are invisible.

### 9.2 No analytics

No screen tracking, no event tracking. No way to know which features users use, where they drop off in the booking funnel, or how search is performing.

**Fix:** Wire analytics events through effect observers in each screen. Screen views via `LaunchedEffect(Unit)` on each screen entry.

---

## Section 10 — Module Dependency Hygiene

### 10.1 `core:common` unnecessarily depends on Compose

`core/common/build.gradle.kts` depends on `androidx.ui`. `MviBase.kt` contains zero composables — it only needs `lifecycle-viewmodel-ktx` and `kotlinx-coroutines-core`. The Compose dependency increases compile scope for every module that depends on `core:common`, including pure Kotlin modules.

### 10.2 Deprecated `TripType.values()` usage

`FlightSearchScreen.kt` uses `TripType.values()` which is deprecated since Kotlin 1.9.

**Fix:** Replace with `TripType.entries` — allocation-free, returns `EnumEntries<TripType>`.

### 10.3 `core:database` is declared but completely empty

Room dependency declared in `build.gradle.kts` but no DAOs, entities, or database class exist.

**Fix:** Either implement Room for offline caching (bookings, recent searches) or remove the module until it is needed. Dead modules in a build graph have a compile-time cost.

---

## Priority Tracker

### P0 — Critical (Block Production Release)

| ID | Gap | File(s) | Status |
|---|---|---|---|
| G-01 | No `DataResult<T>` wrapper — errors silently swallowed | All `*RepositoryImpl`, `HomeViewModel`, `FlightSearchScreen` | `[ ]` |
| G-02 | Zero test coverage — no ViewModel or repository tests | All `feature/*/test/` | `[ ]` |
| G-03 | `HttpLoggingInterceptor(BODY)` in all builds — leaks auth tokens | `core/network/.../NetworkModule.kt` | `[ ]` |
| G-04 | No R8/ProGuard rules — Moshi/Retrofit stripped in release | `app/proguard-rules.pro` | `[ ]` |

---

### P1 — High (Required Before Beta)

| ID | Gap | File(s) | Status |
|---|---|---|---|
| G-05 | Unsafe mutable cast in `NavigationState` | `NavigationState.kt:43` | `[ ]` |
| G-06 | `NavigationState` not saved on process death | `TravelMonkApp.kt` | `[ ]` |
| G-07 | `BookingConfirmationScreen` lives in app module | `TravelEntryProvider.kt:72` | `[ ]` |
| G-08 | `GlobalNavigator` accumulates all feature navigators — SRP violation | `GlobalNavigator.kt` | `[ ]` |
| G-09 | No deeplink support | `TravelEntryProvider.kt` | `[ ]` |
| G-10 | No Use Case / Interactor layer — ViewModels call repos directly | All `*ViewModel.kt` | `[ ]` |
| G-11 | Domain models defined inside MVI files | `ExperienceMvi.kt`, `BookingMvi.kt` | `[ ]` |
| G-12 | All user-visible strings hardcoded — zero `strings.xml` usage | All `*Screen.kt` | `[ ]` |
| G-13 | `collectAsState()` instead of `collectAsStateWithLifecycle()` | All screen composables | `[ ]` |
| G-14 | `LaunchedEffect(Unit)` for effect collection ignores lifecycle | All screen composables | `[ ]` |

---

### P2 — Medium (Quality & Maintainability)

| ID | Gap | File(s) | Status |
|---|---|---|---|
| G-15 | `core:ui` is empty — no shared components | `core/ui/` | `[ ]` |
| G-16 | Design tokens bypassed — hardcoded dp/sp/color values throughout | `HomeScreen.kt`, `FlightSearchScreen.kt`, others | `[ ]` |
| G-17 | `core:tokens` orphaned — icons outside `core:designsystem` | `core/tokens/` | `[ ]` |
| G-18 | Unnecessary coroutine wrapping for synchronous `setState` | `HomeViewModel.kt:24` | `[ ]` |
| G-19 | `SwapCities` reads `currentState` instead of intent snapshot | `FlightViewModel.kt:18` | `[ ]` |
| G-20 | No `Dispatchers.IO` for repository/network calls | All `*ViewModel.kt` | `[ ]` |
| G-21 | `contentDescription = null` throughout — breaks accessibility | All screen composables | `[ ]` |
| G-22 | Flight search results discarded — loaded twice | `FlightViewModel.kt`, `TravelEntryProvider.kt` | `[ ]` |

---

### P3 — Low (Production Polish)

| ID | Gap | File(s) | Status |
|---|---|---|---|
| G-23 | No build flavors — no debug/staging/release environment separation | `app/build.gradle.kts`, `NetworkModule.kt` | `[ ]` |
| G-24 | No crash reporting or analytics | Entire project | `[ ]` |
| G-25 | `core:database` declared but empty — dead module or implement it | `core/database/` | `[ ]` |
| G-26 | `core:common` has unnecessary Compose dependency | `core/common/build.gradle.kts` | `[ ]` |
| G-27 | Deprecated `TripType.values()` — use `TripType.entries` | `FlightSearchScreen.kt` | `[ ]` |
| G-28 | No certificate pinning on booking/payment endpoints | `NetworkModule.kt` | `[ ]` |
| G-29 | No feature flag support — all entries always registered | `TravelEntryProvider.kt` | `[ ]` |

---

## Gap Count Summary

| Priority | Total | Fixed | Remaining |
|---|---|---|---|
| P0 — Critical | 4 | 0 | 4 |
| P1 — High | 10 | 0 | 10 |
| P2 — Medium | 8 | 0 | 8 |
| P3 — Low | 7 | 0 | 7 |
| **Total** | **29** | **0** | **29** |
