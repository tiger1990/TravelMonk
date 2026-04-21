# TravelMonk — Architecture Review & Gaps

> Reviewed by: Senior Principal Architect
> Date: 2026-04-02
> Status legend: `[ ]` open · `[~]` in progress · `[x]` done

---

## Overall Assessment

The foundation is solid. MVI, convention plugins, feature modularization, navigator abstraction,
and the design system token system are all pointing in the right direction.
Below is a full breakdown of remaining gaps that must be resolved before a beta release.

---

## What Is Working Well

| Area | What's Right |
|---|---|
| **MVI** | `BaseViewModel<S,I,E>` enforces unidirectional data flow. `Channel`-based effects guarantee exactly-once delivery — no replay on recomposition |
| **Convention plugins** | Single `travelmonk.android.feature` plugin wires Compose + Hilt + all core deps with zero boilerplate per feature |
| **Feature structure** | Every feature consistently owns `mvi/`, `ui/`, `domain/model/`, `domain/repository/`, `domain/usecase/`, `data/api/`, `data/local/`, `data/repository/`, `navigation/`, `di/` — predictable, scalable |
| **Navigator abstraction** | Features depend on typed navigator interfaces, not on the app module. `NavigationBus` + `NavigationRegistry` dispatches commands to `NavKeyHandler` multibindings — zero coupling between features |
| **Transport multibindings** | `@IntoSet` Hilt multibindings let flights/bus/train register tabs without modifying the transport module — genuine plugin architecture |
| **Stateful/Stateless split** | Every screen separates the hiltViewModel entry point from previewable stateless content |
| **Design system tokens** | `staticCompositionLocalOf` for immutable tokens, `CompositionLocalProvider` for dark/light-aware colors — correct use of Compose primitives |
| **Per-tab back stacks** | Each bottom-bar tab maintains isolated navigation history — standard mobile UX done right |
| **transport-api module** | Interface segregation between `feature:transport` (consumer) and `feature:flights` (provider) via a dedicated contract module |

---

## Section 1 — Navigation

### 1.1 No deeplink support

`entryProvider` has no URI routing. Push notifications, web redirects, and widget taps all require deeplinks. Retrofitting this later across a populated entry provider is expensive.

**Fix:** Define a URI scheme (`travelmonk://flights/results?from=DEL&to=BOM`) and map URIs to `TravelNavKey` instances. Wire into `entryProvider` and declare `<intent-filter>` in `AndroidManifest.xml`.

### 1.2 No feature flag support

All feature entries are always registered in `TravelEntryProvider`. There is no mechanism to conditionally include/exclude a feature for A/B testing, gradual rollout, or per-user segment control.

**Fix:** Introduce a `FeatureFlags` interface injected into `TravelEntryProvider`. Gate each `entryProvider` registration behind a flag check.

---

## Section 2 — Security

### 2.1 No certificate pinning

Booking and payment API endpoints should use OkHttp `CertificatePinner` to prevent MITM attacks.

**Fix:** Add `CertificatePinner` to `OkHttpClient` in `NetworkModule` for booking and payment hosts.

---

## Section 3 — Localization

### 3.1 All user-visible strings hardcoded in Kotlin

`"Hello Traveler,"`, `"Search Flights"`, `"Where to next?"`, `"Refer & Earn"` — zero `strings.xml` usage across 7 features. Making this app available in Hindi, Spanish, or Arabic requires a full rewrite of every screen.

**Fix:** Move all user-visible strings to `res/values/strings.xml` in each feature module. Use `stringResource(R.string.*)` in composables.

---

## Section 4 — Observability

### 4.1 No crash reporting

No Firebase Crashlytics or Sentry integration. In production, crashes are invisible.

### 4.2 No analytics

No screen tracking, no event tracking. No way to know which features users use, where they drop off in the booking funnel, or how search is performing.

**Fix:** Wire analytics events through effect observers in each screen. Screen views via `LaunchedEffect(Unit)` on each screen entry.

---

## Section 5 — Module Dependency Hygiene

### 5.1 `core:database` is declared but completely empty

Room dependency declared in `build.gradle.kts` but no DAOs, entities, or database class exist.

**Fix:** Either implement Room for offline caching (bookings, recent searches) or remove the module until it is needed. Dead modules in a build graph have a compile-time cost.

### 5.2 No DTO layer in API interfaces

`FlightsApi` and other API interfaces return domain models directly (`List<Flight>`) instead of DTOs. This couples the network layer to the domain layer — any API field rename breaks the domain model.

**Status:** DTO layer introduced across all 6 features (`data/api/dto/`) with mappers (`data/mapper/`). API interfaces now return DTOs. Repositories return fake data via `FooDto(...).toDomain()` until real backend is integrated.

**To integrate real backend (per feature):**
1. Uncomment the real API call in `*RepositoryImpl` and delete the `fake*()` method
2. Enhance field mappings in `*Mapper.kt` to match actual API response shape
3. Add `@SerialName` adjustments in `*Dto.kt` if API field names differ

---

## Gap Tracker — Required Before Beta

| ID | Section | Gap | File(s) | Status |
|---|---|---|---|---|
| G-01 | 1.1 | No deeplink support | `TravelEntryProvider.kt` | `[ ]` |
| G-02 | 1.2 | No feature flag support — all entries always registered | `TravelEntryProvider.kt` | `[x]` |
| G-03 | 2.1 | No certificate pinning on booking/payment endpoints | `NetworkModule.kt` | `[ ]` |
| G-04 | 3.1 | All user-visible strings hardcoded — zero `strings.xml` usage | All `*Screen.kt` | `[ ]` |
| G-05 | 4.1 | No crash reporting or analytics | Entire project | `[ ]` |
| G-06 | 5.1 | `core:database` declared but empty — implement or remove | `core/database/` | `[ ]` |
| G-07 | 5.2 | No DTO layer — API interfaces return domain models directly | All `data/api/*.kt` | `[~]` |

---

## Gap Count Summary

| Total Open |
|---|
| **5** |
