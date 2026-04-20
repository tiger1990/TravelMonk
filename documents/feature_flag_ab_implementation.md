# Feature Flag & A/B Testing — TravelMonk

## 1. Overview

Feature flags and A/B testing let TravelMonk ship code continuously while controlling what
users see — independently of Play Store release cycles.

**Goals:**
- Roll out new features gradually (e.g. 5% → 20% → 100%) without a new app release
- Run experiments on UI variants, onboarding flows, and pricing surfaces
- Instantly kill/rollback a broken feature without redeployment
- Resolve Architecture Gap 1.2 (No feature flag support)

---

## 2. SDK Comparison

> Legend: ✅ Strong  ⚠️ Partial / limited  ❌ Weak / not applicable

| Tool                      | Focus                            | Android SDK            | Feature Flags         | Stats Depth           | Analytics             | OSS | Pricing                 | TravelMonk Fit        |
|---------------------------|----------------------------------|------------------------|-----------------------|-----------------------|-----------------------|-----|-------------------------|-----------------------|
| **Amplitude Experiment**  | Experimentation + analytics      | ✅ Native (<200 KB)    | ✅ Yes                | ✅ CUPED + holdouts   | ✅ Native (funnels, replay) | ❌  | Free tier + paid        | ⭐⭐⭐ Best analytics |
| **Firebase A/B Testing**  | Basic A/B on Remote Config       | ✅ Native              | ✅ Remote Config      | ⚠️ Basic only         | ⚠️ Firebase Analytics | ❌  | Free                    | ⭐⭐⭐ Easiest setup  |
| **GrowthBook**            | Open-source experimentation      | ✅ Android SDK         | ✅ Yes                | ⚠️ Basic              | ⚠️ Warehouse-native   | ✅  | Free OSS + free cloud   | ✅ **Recommended**    |
| **Statsig**               | Statistical experimentation      | ✅ Mobile SDKs         | ✅ Yes                | ✅ CUPED + sequential | ⚠️ Warehouse-native   | ❌  | Free tier + paid        | ⚠️ Needs data eng    |
| **LaunchDarkly**          | Feature flag management          | ✅ Native              | ✅ Best-in-class      | ⚠️ Basic add-on       | ❌ Separate tools      | ❌  | Seat-based (expensive)  | ⚠️ Flags only        |
| **Split.io**              | Feature delivery / safe releases | ✅ Mobile SDKs         | ✅ Yes                | ⚠️ Basic              | ❌ Separate tools      | ❌  | Seat-based              | ⚠️ DevOps-focused    |
| **Optimizely**            | Enterprise experimentation       | ✅ Full-stack SDKs     | ✅ Yes                | ⚠️ Standard           | ❌ Separate tools      | ❌  | Enterprise (expensive)  | ❌ Overkill           |
| **AB Tasty**              | Multi-channel (web + mobile)     | ⚠️ Mobile SDKs        | ⚠️ Basic              | ⚠️ Basic              | ❌ Limited             | ❌  | Mid-market              | ❌ Generic            |
| **VWO**                   | Web CRO (mobile secondary)       | ⚠️ Mobile SDKs        | ⚠️ Limited            | ⚠️ Basic              | ❌ Limited             | ❌  | Paid                    | ❌ Web-first          |

---

## 3. Decision Criteria for TravelMonk

| Criterion | Weight | Notes |
|---|---|---|
| Kotlin / Android native SDK | Must-have | Project is 100% Kotlin |
| Hilt DI compatible | Must-have | Inject `FeatureFlagClient` via `@Provides` |
| MVI architecture fit | Must-have | Flags must integrate cleanly with State/Intent flow |
| Lightweight SDK (<500KB) | High | Keep APK size minimal |
| Free / low cost | High | Personal dev project, no budget |
| Feature flags (no Play Store resubmission) | Must-have | Core requirement |
| Basic A/B testing | Nice-to-have | Useful for UX experiments |
| Simple setup (no mandatory self-hosted backend) | High | Prefer managed or in-app config |

---

## 4. Decision: Statsig

**Chosen SDK: Statsig** — Android-native Kotlin SDK, free tier, CUPED + sequential testing,
offline cache built-in, and a clean API that maps well to the existing `AppConfig` pattern.

| Factor                     | Statsig verdict                                                    |
|----------------------------|--------------------------------------------------------------------|
| Android / Kotlin SDK       | ✅ First-class (`com.statsig:android-sdk:4.37.1` on Maven Central) |
| Hilt DI compatible         | ✅ Singleton, easy to wrap in `@Provides`                          |
| Feature flags (gates)      | ✅ `Statsig.checkGate("flag_name")`                                |
| A/B / experiments          | ✅ `Statsig.getExperiment()` + `getLayer()`                        |
| Statistical depth          | ✅ CUPED, sequential testing, MCC                                  |
| Offline support            | ✅ SDK caches last-known values locally, serves from cache         |
| Free tier                  | ✅ Generous free tier on Statsig Cloud                             |
| Setup complexity           | ✅ Single `initialize()` call in `Application` class               |
| Open source                | ⚠️ Not OSS (but free tier is sufficient)                          |

---

## 5. Architecture Opinion — Where to put FeatureFlags in `core:common`

**Short answer: YES — interface + enum in `core:common`, implementation in `app`.**

This follows the **exact same pattern** already established by `AppConfig`:

```
core/common  →  interface AppConfig          (no SDK dependency)
app          →  anonymous object : AppConfig  (wired in AppModule.kt)
```

Apply the same split for feature flags:

```
core/common  →  interface FeatureFlagRepository   (no Statsig import)
core/common  →  enum class FeatureFlag            (flag key registry)
app          →  class StatsigFeatureFlagRepository (wraps Statsig SDK)
app          →  AppModule.kt @Provides binding
```

**Why this is right:**
- Every feature ViewModel needs flag access → interface must live in a shared module
- `core:common` is already the home for cross-cutting contracts (`AppConfig`, `DataResult`, `BaseViewModel`)
- The Statsig SDK dependency stays **only in `app/build.gradle.kts`** — no SDK bleeds into feature modules
- Swapping SDKs later only touches `app` — zero changes to feature modules

**What NOT to do:**
- ❌ Don't add `com.statsig:android-sdk` to `core/common/build.gradle.kts` — that pulls the SDK into every module
- ❌ Don't inject `Statsig` singleton directly into ViewModels — always go through the interface
- ❌ Don't create a separate `core:flags` module yet — premature; `AppConfig` doesn't have its own module

---

## 6. Statsig SDK Setup

### 6.1 Dependency — `app/build.gradle.kts`

```kotlin
dependencies {
    implementation("com.statsig:android-sdk:4.37.1")
}
```

### 6.2 SDK Initialisation — `TravelMonkApplication.kt`

Initialize once in `Application.onCreate()`. The SDK caches values locally, so subsequent
calls serve from cache while a fresh fetch runs in the background.

```kotlin
class TravelMonkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Statsig — non-blocking, serves cached values immediately
        CoroutineScope(Dispatchers.IO).launch {
            Statsig.initialize(
                application = this@TravelMonkApplication,
                sdkKey      = BuildConfig.STATSIG_CLIENT_KEY,
                user        = StatsigUser(userID = "anonymous"), // update after login
            )
        }
    }
}
```

> `BuildConfig.STATSIG_CLIENT_KEY` — add `STATSIG_CLIENT_KEY=client-xxxx` to `local.properties`
> and expose it via `buildConfigField` in `app/build.gradle.kts`. Never hardcode the key.

---

## 7. Interface & Implementation

### 7.1 `FeatureFlag` enum — `core/common`

```kotlin
// core/common/src/main/java/com/travelmonk/core/common/flags/FeatureFlag.kt
//package com.travelmonk.core.common.flags

enum class FeatureFlag(val key: String) {
    PROMO_BANNER_HOME("promo_banner_home"),
    NEW_FLIGHT_SEARCH_UI("new_flight_search_ui"),
    EXPERIMENTAL_BOOKING_FLOW("experimental_booking_flow"),
}
```

### 7.2 `FeatureFlagRepository` interface — `core/common`

```kotlin
// core/common/src/main/java/com/travelmonk/core/common/flags/FeatureFlagRepository.kt
//package com.travelmonk.core.common.flags

interface FeatureFlagRepository {
    fun isEnabled(flag: FeatureFlag): Boolean
    fun getString(flag: FeatureFlag, default: String): String
    fun getDouble(flag: FeatureFlag, default: Double): Double
}
```

### 7.3 `StatsigFeatureFlagRepository` implementation — `app`

```kotlin
// app/src/main/java/com/travelmonk/flags/StatsigFeatureFlagRepository.kt
//package com.travelmonk.flags

/**
import com.statsig.androidsdk.Statsig
import com.travelmonk.core.common.flags.FeatureFlag
import com.travelmonk.core.common.flags.FeatureFlagRepository
 */

class StatsigFeatureFlagRepository : FeatureFlagRepository {

    override fun isEnabled(flag: FeatureFlag): Boolean =
        Statsig.checkGate(flag.key)

    override fun getString(flag: FeatureFlag, default: String): String =
        Statsig.getConfig(flag.key).getString(flag.key, default)

    override fun getDouble(flag: FeatureFlag, default: Double): Double =
        Statsig.getConfig(flag.key).getDouble(flag.key, default)
}
```

### 7.4 Hilt Binding — `app/di/AppModule.kt`

Follows the exact same pattern as `AppConfig`:

```kotlin
@Provides
@Singleton
fun provideFeatureFlagRepository(): FeatureFlagRepository =
    StatsigFeatureFlagRepository()
```

---

## 8. MVI Integration Example

### HomeState

```kotlin
@Immutable
data class HomeState(
    val isLoading: Boolean = false,
    val banners: ImmutableList<Banner> = persistentListOf(),
    val isPromoBannerEnabled: Boolean = false,   // driven by Statsig gate
)
```

### HomeViewModel

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeBannersUseCase: GetHomeBannersUseCase,
    private val featureFlags: FeatureFlagRepository,
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    init {
        updateState {
            copy(isPromoBannerEnabled = featureFlags.isEnabled(FeatureFlag.PROMO_BANNER_HOME))
        }
    }
}
```

### HomeContent (stateless composable)

```kotlin
@Composable
fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Flag value arrives via State — no direct SDK call in Composable
    if (state.isPromoBannerEnabled) {
        PromoBanner()
    }
}
```

---

## 9. User Identity — Update After Login

Statsig gates can target authenticated users. Update the user once login completes:

```kotlin
// In AuthViewModel or wherever login succeeds
viewModelScope.launch {
    Statsig.updateUser(StatsigUser(userID = loggedInUserId))
}
```

---

## 10. Implementation Checklist

- [ ] Add `STATSIG_CLIENT_KEY` to `local.properties` and `buildConfigField` in `app/build.gradle.kts`
- [ ] Add `com.statsig:android-sdk:4.37.1` to `app/build.gradle.kts`
- [ ] Initialize Statsig in `TravelMonkApplication.onCreate()`
- [ ] Create `FeatureFlag` enum in `core/common/.../flags/`
- [ ] Create `FeatureFlagRepository` interface in `core/common/.../flags/`
- [ ] Create `StatsigFeatureFlagRepository` in `app/.../flags/`
- [ ] Add `@Provides` binding in `app/di/AppModule.kt`
- [ ] Inject `FeatureFlagRepository` into `HomeViewModel`, gate promo banner
- [ ] Update `ArchitectureGaps.md` — mark Gap 1.2 `[x]` done

---

## 11. Open Questions

| Question                                    | Decision                                              |
|---------------------------------------------|-------------------------------------------------------|
| Which gate to create first in Statsig console? | `promo_banner_home` on `HomeScreen`                |
| Update `StatsigUser` after login?           | Yes — call `Statsig.updateUser()` in auth success     |
| Offline behaviour?                          | SDK serves cached values automatically               |
| Flag evaluation: app start vs on-demand?    | App start + cache in memory; re-evaluate after user update |
