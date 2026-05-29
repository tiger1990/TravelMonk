# TravelMonk — Pending Work Tracker

> Consolidated from: `ArchitectureGaps.md`, `gaps.md`, `OnboradingModuleReview.md`
> Sources use `[x]` / status fields as authoritative completion markers.
> Status legend: `[ ]` = todo · `[~]` = in progress

---

## Completed (Do Not Re-Open)

| Item | Source | What Was Fixed |
|------|--------|----------------|
| NavigationState process death | GAP-01 | Backed by ViewModel + SavedStateHandle |
| BookingType enum crash | GAP-02 | Deleted local enum, use core model directly |
| Silent error swallowing | GAP-03 | DataResult<T> + error state in all 4 ViewModels |
| FlightRepositoryImpl @Singleton | GAP-09 | Added @Singleton annotation |
| HTTP Logging in release builds | GAP-12 | Gated behind appConfig.isDebug |
| SharedFlow overflow strategy | GAP-16 | Changed to DROP_LATEST |
| Feature flag support for nav | ArchitectureGaps G-02 | FeatureFlags interface gates entryProvider |
| DTO layer introduced | ArchitectureGaps G-07 | All 6 features have data/api/dto/ + mappers |
| Onboarding module — all 14 issues | OnboradingModuleReview.md | See that file for full detail |
| collectAsState → collectAsStateWithLifecycle | GAP-06, gaps.md G-13 | Already done in all 15 screens |
| withContext(Dispatchers.IO) in repositories | gaps.md G-20 | All 5 repos use @IoDispatcher + withContext |
| FlightViewModel race conditions | gaps.md G-19 | SwapCities uses setState lambda; SearchFlights snapshots before launch |
| StayDetailsViewModel → GetStayDetailsUseCase | GAP-05 | Created use case; StayDetailsViewModel now clean-architecture compliant |
| SavedStateHandle in feature ViewModels | GAP-04 / P1-01 | FlightVM (7 keys), StayVM (2), ExperienceVM (1), TransportVM (1); enum fields serialized as .name |
| NavigationState decoupled from feature nav keys | GAP-07 / P1-02 | BottomBarItem.all drives tab stacks; NavigationState has zero feature imports |
| Cross-feature nav keys removed from feature NavigatorModules | GAP-08 / P1-03 | 5 feature NavigatorModule files deleted; all providers consolidated in app/di/NavigationModule.kt |
| Transport typealias shim deleted | GAP-10 / P1-10 | Shim file deleted; TransportMvi.kt imports real TransportTab from feature/transport-api |
| Design system onImage/imageScrim tokens + HomeScreen | GAP-11 / P2-01 (partial) | Tokens added to TravelMonkColors; HomeScreen Color.White/Black replaced. MyBookingsScreen, ExperiencesScreen, FlightSearchScreen still pending. |
| Repository Flow return types | GAP-13 / P2-02 | Booking, Home, Services repos: suspend→Flow<DataResult<>>; use cases pass through; VMs use collect |

---

## P0 — Critical (Block Production Release)

### P0-01 · Zero test coverage
- **Source:** gaps.md G-02
- **Status:** `[~]` Partial
- **Done:** `core/testing/MainDispatcherRule.kt` exists. `FlightViewModelTest` + `FakeFlightRepository` + `FlightFixtures` written. Onboarding module has 5 tests (OtpViewModel, PhoneEntryViewModel, PasskeyPromptViewModel, VerifyOtpUseCase, OtpRateLimiter, KeyRotationManager, SessionDataSerializer).
- **Remaining:** `HomeViewModelTest`, `ExperienceViewModel` tests, and 5 other feature VMs (stays, bookings, services, transport) still have empty test directories.

---

### ~~P0-02 · No R8/ProGuard rules — release build crashes~~ ✅ Done
- **Source:** gaps.md G-04
- **Fixed:** `app/proguard-rules.pro` now has comprehensive rules: Navigation3 NavKey FQCN preservation (process-death restore), Hilt annotations, `@SerialName` fields for kotlinx.serialization, Retrofit interface methods, and Coil. Readable stack traces preserved via `SourceFile`/`LineNumberTable`.

---

## P1 — High (Required Before Beta)

### ~~P1-01 · SavedStateHandle missing in all main-feature ViewModels~~ ✅ Done
- **Source:** GAP-04
- **Fixed:** FlightVM (7 keys), StayVM (2 keys), ExperienceVM (1 key), TransportVM (1 key). Enum fields serialized as `.name` with `entries.firstOrNull` safe deserialization. `initialDataLoad()` in ExperienceVM now uses `currentState.selectedCategory` instead of hardcoded default.

### ~~P1-02 · `NavigationState` imports all feature API nav keys (direct coupling)~~ ✅ Done
- **Source:** GAP-07
- **Fixed:** `BottomBarItem` now holds `navTab: NavTab` + `companion object { val all }`. `NavigationState` uses `BottomBarItem.all.associate { it.route to rememberNavBackStack(it.route) }` — zero feature nav key imports.

### ~~P1-03 · Cross-feature nav key imports in feature navigators~~ ✅ Done
- **Source:** GAP-08
- **Fixed:** All 5 feature `NavigatorModule.kt` files deleted. Providers consolidated into `app/di/NavigationModule.kt` companion object (Option B applied — app layer resolves cross-feature keys).

### ~~P1-04 · Unsafe mutable cast in `NavigationState`~~ ✅ Done
- **Source:** gaps.md G-05
- **Fixed:** `NavigationState` calls `stack.add(key)` and `stack.removeLastOrNull()` directly on `NavBackStack<NavKey>`. The `(backStack as MutableList<TravelNavKey>).add(key)` pattern is gone.

### ~~P1-05 · `BookingConfirmationScreen` lives in app module~~ ✅ Done
- **Source:** gaps.md G-07
- **Fixed:** `BookingConfirmationScreen` is now in `feature/bookings/src/main/java/com/travelmonk/feature/bookings/ui/BookingConfirmationScreen.kt`. App module only wires navigation.

### ~~P1-06 · `GlobalNavigator` violates SRP~~ ✅ Done
- **Source:** gaps.md G-08
- **Fixed:** `GlobalNavigator` now only implements `NavigationBus` with `navigate(key, options)` and `back()`. Feature navigator implementations are thin anonymous objects in `app/di/NavigationModule.kt` — each delegates to `NavigationBus`. No feature-specific overrides accumulate in `GlobalNavigator`.

### P1-07 · No deep link support
- **Source:** GAP-14, gaps.md G-09
- **Where:** `MainActivity.kt`, `TravelEntryProvider.kt`
- **Fix:** Define URI scheme (`travelmonk://flights/results?from=DEL&to=BOM`). Handle `intent` in `onCreate`/`onNewIntent`. Route parsed path/params to `NavigationBus` as `TravelNavKey`. Add `<intent-filter>` in `AndroidManifest.xml`. Define `DeepLinkHandler` in `core/navigation`.
- **Status:** `[ ]`

### ~~P1-08 · Domain models defined inside MVI files~~ ✅ Done
- **Source:** gaps.md G-11
- **Fixed:** `BookingItem` is in `feature/bookings/domain/model/BookingItem.kt` (imported in MVI, not defined there). `ExperienceMvi.kt` uses `Experience` from `feature/experiences/domain/model/`. Both MVI files now contain only `State`, `Intent`, `Effect`.

### ~~P1-09 · All user-visible strings hardcoded in main feature screens~~ ✅ Done
- **Source:** gaps.md G-12, ArchitectureGaps G-04
- **Fixed:** Created `strings.xml` for 6 feature modules (home, flights, stays, experiences, bookings, transport). All user-visible string literals replaced with `stringResource(R.string.*)` across 10 screen composables. Services already had strings.xml.

### ~~P1-10 · Backwards-compatibility typealias shim in Transport module~~ ✅ Done
- **Source:** GAP-10
- **Fixed:** `TransportTabContentProvider.kt` deleted. `TransportMvi.kt` import updated to `com.travelmonk.feature.transportapi.TransportTab`.

---

## P2 — Medium (Quality & Maintainability)

### ~~P2-01 · Hardcoded color literals violate design system rules~~ ✅ Done
- **Source:** GAP-11, gaps.md G-16
- **Fixed:** All four screens now clean. `MyBookingsScreen.kt`, `ExperiencesScreen.kt` (uses `TravelYellow` token from `core.design.system.color`), and `FlightSearchScreen.kt` contain zero `Color(0xFF...)` or `Color.White/Black` literals. `HomeScreen.kt` was already done previously.

### ~~P2-02 · Repositories return `List<T>` instead of `Flow<List<T>>`~~ ✅ Done
- **Source:** GAP-13
- **Fixed:** `BookingRepository`, `HomeRepository`, `ServiceRepository` — `suspend fun` → `Flow<DataResult<List<T>>>`. Implementations use `flow { emit(...) }.flowOn(ioDispatcher)`. Use cases pass through. ViewModels use `.collect { }`. Parameterized searches and mutations kept as `suspend`.

### ~~P2-03 · `core:ui` is empty — no shared components~~ ✅ Done
- **Source:** gaps.md G-15
- **Fixed:** `core/ui` now has `TravelMonkButton`, `TravelMonkCard`, `TravelMonkTextField`, `TravelMonkTopBar`, `LocalNavContentPadding`, `LocalFeatureFlags`, `LogScreenLifecycle`, `TravelMonkSnackbar`. All features use these shared components.

### P2-04 · `core:tokens` is an orphaned module
- **Source:** gaps.md G-17
- **Where:** `TravelMonkIcons` in `core:tokens`, separate from `core:designsystem`
- **Fix:** Move `TravelMonkIcons` into `core:designsystem/icons/`. Features should not depend on two separate design modules.
- **Status:** `[ ]`

### ~~P2-05 · `setState` unnecessarily wrapped in coroutine~~ ✅ Done
- **Source:** gaps.md G-18
- **Fixed:** `HomeViewModel` was fully refactored to a reactive `stateIn` pipeline — no manual `setState` calls remain at all. State transitions happen automatically via `map { }` on the use case Flow.

### P2-06 · Accessibility — `contentDescription = null` throughout
- **Source:** gaps.md G-21
- **Where:** Every icon in `HomeScreen.kt`, `FlightSearchScreen.kt`, `TransportUI.kt`, etc.
- **Fix:** All interactive icons need meaningful `contentDescription`. Decorative-only icons use `null` with explicit intent comment.
- **Status:** `[ ]`

---

## P3 — Low (Production Polish)

### ~~P3-01 · No build flavors for environment separation~~ ✅ Done
- **Source:** gaps.md G-22
- **Fixed:** Added `environment` flavor dimension with 3 flavors (`dev`/`staging`/`production`). Removed the old `staging` build type that conflated environment with build behaviour. 6 variants: devDebug, devRelease, stagingDebug, stagingRelease, productionDebug, productionRelease. `AppConfig` extended with `environment: Environment` enum and `apiTimeoutSeconds: Int`. `NetworkModule` wires timeouts (15s prod / 30s dev+staging). Flavor source sets override `app_name` for dev and staging builds.

### P3-02 · No crash reporting or analytics
- **Source:** gaps.md G-23, ArchitectureGaps G-05
- **Where:** Entire project
- **Fix:** Integrate Firebase Crashlytics for crashes. Wire analytics events through effect observers per screen.
- **Status:** `[ ]`

### P3-03 · Room integration — `core:database` declared but empty
- **Source:** gaps.md G-24, ArchitectureGaps G-06
- **Where:** `core/database/` module — Room deps declared, zero DAOs/entities
- **Fix:** Implement Room for at minimum: bookings (offline read), recent searches. Or remove the module to eliminate dead compile graph weight.
- **Status:** `[ ]`

### ~~P3-04 · `core:common` unnecessarily pulls in Compose~~ ✅ Done
- **Source:** gaps.md G-25
- **Fixed:** `core/common/build.gradle.kts` depends only on `lifecycle-runtime-ktx`, `kotlinx-coroutines-core`, `datastore-preferences`, and `core:logger`. Zero Compose dependencies.

### ~~P3-05 · `TripType.values()` deprecated since Kotlin 1.9~~ ✅ Done
- **Source:** gaps.md G-26
- **Fixed:** Both `FlightSearchScreen.kt` (line 178) and `FlightViewModel.kt` (line 28) now use `TripType.entries`.

### P3-06 · No certificate pinning on booking/payment endpoints
- **Source:** ArchitectureGaps G-03
- **Where:** `core/network/.../NetworkModule.kt`
- **Fix:** Add `CertificatePinner` to `OkHttpClient` for booking and payment hosts.
- **Status:** `[ ]`

---

## Summary

| Priority | Total Pending |
|----------|---------------|
| P0 — Critical | 1 (partial: test coverage) |
| P1 — High | 1 |
| P2 — Medium | 2 |
| P3 — Low | 2 |
| **Total** | **6** |

> **Note:** GAP-15 / gaps.md G-14 (LaunchedEffect(Unit) for effect collection) is intentionally excluded — `todo.md` explicitly marks it as a non-gap: `LaunchedEffect(Unit)` is the project-endorsed pattern per `compose-ui-best-practice.md`.

### Recommended Sprint Order
1. **P0-01** — Test coverage (unblocks safe refactoring of everything below)
2. **P0-02** — ProGuard rules (release build crashes without this)
3. **P1-04** — Unsafe mutable cast (small, low risk)
4. **P1-09** — Strings in main feature screens (systematic but mechanical)
5. **P1-08** — Domain models out of MVI files (refactor with tests passing)
6. **P1-05 + P1-06 + P1-07** — Navigation structure (BookingConfirmation, GlobalNavigator, deep links)
7. **P2-05** — setState coroutine wrapping (quick fix)
8. **P2-01** — Design system token sweep (3 screens remain)
9. **P2-03 + P2-04** — core:ui + core:tokens consolidation
10. **P2-06** — Accessibility
11. **P3-01 through P3-06** — Production polish
