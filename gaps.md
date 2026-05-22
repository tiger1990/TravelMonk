# TravelMonk — Architectural Gaps Tracker - Before Beta Check All Once Again

> Reviewed by: Senior Principal Architect
> Status legend: `[ ]` open · `[~]` in progress · `[x]` fixed

---

## P0 — Critical (Block Production Release)

### G-01 · Error Handling — No `Result<T>` wrapper, errors silently swallowed
- **Where:** All repositories catch exceptions and return mock data; `HomeViewModel` sets `error` in state but `HomeContent` never reads it; `FlightEffect.ShowError` handler body is empty
- **Files:** `HomeViewModel.kt`, `HomeContent` in `HomeScreen.kt`, `FlightSearchScreen.kt:42`, all `*RepositoryImpl.kt`
- **Fix:** Introduce `sealed class DataResult<T>` (Success / Error / Loading). Repositories return `DataResult`. ViewModels map it to state. Screens render error UI.
- **Status:** `[ ]`

---

### G-02 · Testing — Zero test coverage
- **Where:** No ViewModel unit tests, no repository tests, no UI tests beyond generated templates
- **Files:** All `feature/*/test/` directories are empty; no fake/stub repository implementations exist
- **Fix:** Add `MainDispatcherRule`, fake repositories per feature, ViewModel unit tests covering state transitions and effect delivery. Minimum: FlightViewModel, HomeViewModel, ExperienceViewModel.
- **Status:** `[ ]`

---

### G-03 · Security — `HttpLoggingInterceptor` at `BODY` level in all builds
- **Where:** `core/network/.../di/NetworkModule.kt` — `HttpLoggingInterceptor.Level.BODY` logs auth tokens and PII in release builds
- **Fix:** Gate on `BuildConfig.DEBUG`:
  ```kotlin
  val level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
              else HttpLoggingInterceptor.Level.NONE
  ```
- **Status:** `[ ]`

---

### G-04 · Security — No R8/ProGuard rules
- **Where:** No `proguard-rules.pro` entries for Moshi, Retrofit, or Hilt
- **Fix:** Add keep rules for `@JsonClass`, Retrofit interfaces, and Hilt generated components. Without this, release builds will crash at runtime.
- **Status:** `[ ]`

---

## P1 — High (Required Before Beta)

### G-05 · Navigation — Unsafe mutable cast in `NavigationState`
- **Where:** `NavigationState.kt:43` — `(backStack as MutableList<TravelNavKey>).add(key)`
- **Fix:** Each tab stack is already a `SnapshotStateList`. Expose a private accessor per tab that returns the typed list directly, removing the unsafe cast.
- **Status:** `[ ]`

---

### G-06 · Navigation — `NavigationState` not saved on process death
- **Where:** `TravelMonkApp.kt` — `rememberNavigationState()` uses plain `remember`, lost on process kill
- **Fix:** Back `NavigationState` with a ViewModel + `SavedStateHandle`, or use `rememberSaveable` with a custom `Saver`.
- **Status:** `[ ]`

---

### G-07 · Navigation — `BookingConfirmationScreen` lives in app module
- **Where:** `TravelEntryProvider.kt:72` — feature UI has leaked into the app wiring layer
- **Fix:** Move `BookingConfirmationScreen` into `feature:bookings`. App module should only wire, never own composables.
- **Status:** `[ ]`

---

### G-08 · Navigation — `GlobalNavigator` violates SRP, will become a god class
- **Where:** `GlobalNavigator.kt` implements 5 navigator interfaces today; every new feature adds more
- **Fix:** Replace with a `NavigationBus` (or typed key registry) so features push keys to a central handler without GlobalNavigator accumulating `override fun navigateTo(...)` for each feature.
- **Status:** `[ ]`

---

### G-09 · Navigation — No deeplink support
- **Where:** `TravelEntryProvider.kt` — `entryProvider` has no URI routing
- **Fix:** Design URI scheme (`travelmonk://flights/results?from=DEL&to=BOM`) and map to NavKeys inside entryProvider. Required for push notifications, web redirects, and widget taps.
- **Status:** `[ ]`

---

### G-10 · Clean Architecture — No Use Case / Interactor layer
- **Where:** All ViewModels (`HomeViewModel`, `FlightViewModel`, etc.) call repositories directly
- **Fix:** Introduce `domain/usecase/` per feature. ViewModels depend on use cases. Repositories remain hidden behind use cases. Enables composing multiple repos and independent business logic testing.
- **Status:** `[ ]`

---

### G-11 · Clean Architecture — Domain models defined inside MVI files
- **Where:** `ExperienceItem` in `ExperienceMvi.kt`, `BookingItem` in `BookingMvi.kt`
- **Fix:** Move to `feature/*/domain/model/`. MVI files should only contain `State`, `Intent`, `Effect` — no domain data classes.
- **Status:** `[ ]`

---

### G-12 · Localization — All strings hardcoded in Kotlin
- **Where:** Every screen composable — `"Hello Traveler,"`, `"Search Flights"`, `"Where to next?"`, etc.
- **Fix:** Move all user-visible strings to `strings.xml`. Use `stringResource(R.string.*)` in composables. Required for i18n — retrofitting this later across 7+ features is expensive.
- **Status:** `[ ]`

---

### G-13 · Lifecycle — `collectAsState()` instead of `collectAsStateWithLifecycle()`
- **Where:** Every screen — `HomeScreen.kt:33`, `FlightSearchScreen.kt:36`, and all other feature screens
- **Fix:** Replace `collectAsState()` with `collectAsStateWithLifecycle()` from `lifecycle-runtime-compose`. Prevents state collection when app is backgrounded.
- **Status:** `[ ]`

---

### G-14 · Lifecycle — `LaunchedEffect(Unit)` for effect collection ignores lifecycle
- **Where:** Every screen — `HomeScreen.kt:35`, `FlightSearchScreen.kt:38`, etc.
- **Fix:**
  ```kotlin
  LaunchedEffect(viewModel.effect, lifecycleOwner) {
      lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
          viewModel.effect.collect { ... }
      }
  }
  ```
- **Status:** `[ ]`

---

## P2 — Medium (Quality & Maintainability)

### G-15 · Design System — `core:ui` is empty, no shared components
- **Where:** `core/ui/` module declared but contains zero files
- **Fix:** Implement `TravelMonkButton`, `TravelMonkCard`, `TravelMonkTextField` as shared composables. Every feature currently reimplements the same card/button shapes independently.
- **Status:** `[ ]`

---

### G-16 · Design System — Token bypass throughout UI
- **Where:** Hardcoded values across all screens:

  | File | Line | Violation | Should Use |
  |---|---|---|---|
  | `HomeScreen.kt` | 82 | `Modifier.size(28.dp)` | `TravelMonkTheme.dimensions.iconMedium` |
  | `HomeScreen.kt` | 70 | `padding(horizontal = 24.dp, vertical = 32.dp)` | `TravelMonkTheme.spacing.*` |
  | `FlightSearchScreen.kt` | 94 | `offset(y = (-30).dp)` | Named dimension token |
  | `FlightSearchScreen.kt` | 133 | `fontSize = 18.sp, fontWeight = FontWeight.Bold` | `TravelMonkTheme.typography.titleLarge` |
  | `FlightSearchScreen.kt` | 109 | `Color(0xFFF1F4F8)` | Named color token in `Color.kt` |
  | Multiple screens | — | `RoundedCornerShape(16.dp)` | `TravelMonkTheme.radius.medium` |
  | Multiple screens | — | `RoundedCornerShape(32.dp)` | `TravelMonkTheme.radius.extraLarge` |

- **Fix:** Audit all screens, replace every raw dp/sp/color with design system tokens.
- **Status:** `[ ]`

---

### G-17 · Design System — `core:tokens` is an orphaned module
- **Where:** `TravelMonkIcons` lives in `core:tokens`, separate from `core:designsystem`
- **Fix:** Move `TravelMonkIcons` into `core:designsystem/icons/` or `core:designsystem/components/`. Eliminates the need for features to depend on two separate design-related modules.
- **Status:** `[ ]`

---

### G-18 · ViewModel — Unnecessary coroutine wrapping for synchronous `setState`
- **Where:** `HomeViewModel.kt:24` — `viewModelScope.launch { setState { copy(isLoading = true) } }`
- **Fix:** `setState` is a synchronous `MutableStateFlow.value` assignment. Call it directly; only wrap `setEffect` (suspend) in a coroutine.
- **Status:** `[ ]`

---

### G-19 · ViewModel — `SwapCities` reads `currentState` instead of intent snapshot
- **Where:** `FlightViewModel.kt:18` — `fromCode = currentState.toCode` should be `intent.fromCode`
- **Fix:** Always use values carried by the intent inside `handleIntent`. Reading `currentState` inside an intent handler creates a race condition if two intents arrive concurrently.
- **Status:** `[ ]`

---

### G-20 · ViewModel — No `Dispatchers.IO` for repository calls
- **Where:** All ViewModels — `viewModelScope.launch { repo.call() }` defaults to Main dispatcher
- **Fix:** Repositories should internally wrap with `withContext(Dispatchers.IO)`. Callers should not need to know about threading.
- **Status:** `[ ]`

---

### G-21 · Accessibility — `contentDescription = null` throughout
- **Where:** Every icon in `HomeScreen.kt`, `FlightSearchScreen.kt`, `TransportUI.kt`, etc.
- **Fix:** All interactive icons and images need meaningful `contentDescription` values. Decorative-only icons should use `contentDescription = null` only with explicit intent comment.
- **Status:** `[ ]`

---

## P3 — Low (Production Polish)

### G-22 · Build Config — No build flavors for environment separation
- **Where:** `app/build.gradle.kts` — single config, base URL hardcoded in `NetworkModule`
- **Fix:** Add `debug` / `staging` / `release` product flavors. Move base URL to `BuildConfig` fields. Each environment gets its own API endpoint and keys.
- **Status:** `[ ]`

---

### G-23 · Observability — No crash reporting or analytics
- **Where:** Entire project
- **Fix:** Integrate Firebase Crashlytics (crash reporting) and Firebase Analytics or Amplitude (screen tracking). Wire analytics events through effect observers in each screen.
- **Status:** `[ ]`

---

### G-24 · Offline / Caching — `core:database` is declared but empty
- **Where:** `core/database/` module has `build.gradle.kts` with Room deps but zero source files
- **Fix:** Implement Room DAOs for at minimum: bookings (offline read), recent searches (flight/stay). Repositories read from cache, refresh from network.
- **Status:** `[ ]`

---

### G-25 · Module Dependencies — `core:common` unnecessarily pulls in Compose
- **Where:** `core/common/build.gradle.kts` — depends on `androidx.ui` but `MviBase.kt` has no composables
- **Fix:** Remove `androidx.ui` dependency from `core:common`. It only needs `lifecycle-viewmodel-ktx` and `kotlinx-coroutines-core`. Reduces compile scope for pure Kotlin modules that depend on common.
- **Status:** `[ ]`

---

### G-26 · Kotlin — Deprecated `TripType.values()` usage
- **Where:** `FlightSearchScreen.kt` — `TripType.values()` is deprecated since Kotlin 1.9
- **Fix:** Replace with `TripType.entries` (returns `EnumEntries<TripType>`, allocation-free).
- **Status:** `[ ]`

---

### G-27 · Navigation — No feature flag / conditional navigation support
- **Where:** `TravelEntryProvider.kt` — all entries are always registered regardless of user tier or A/B test
- **Fix:** Pass a `FeatureFlagProvider` into `entryProvider`. Premium features (e.g., experiences) only registered for eligible users.
- **Status:** `[ ]`

---

## Summary

| Priority | Total | Fixed |
|---|---|---|
| P0 — Critical | 4 | 0 |
| P1 — High | 10 | 0 |
| P2 — Medium | 8 | 0 |
| P3 — Low | 5 | 0 |
| **Total** | **27** | **0** |
