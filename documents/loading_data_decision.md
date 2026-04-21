# Decision Record: Loading Initial Data in MVI

> Source: [Loading Initial Data properly with MVI — Maxime Michel, ProAndroidDev (Feb 2025)](https://proandroiddev.com/loading-initial-data-properly-with-mvi-5e54edd8ae56)
> See also: [Jaewoong Eum's complementary article](https://proandroiddev.com/loading-initial-data-in-launchedeffect-vs-viewmodel-f1747c20ce62)

This document captures key learnings and the architectural decision for triggering initial data loads in TravelMonk.

---

## 1. The Core Problem

When a screen enters composition, it needs to fetch data. The trigger must:

1. **Survive configuration changes** — not re-fetch on rotation
2. **Be testable** — allow unit test setup between ViewModel init and data loading
3. **Be lifecycle-aware** — only run when the UI actually has an active subscriber
4. **Support lazy init** — not load data for screens that are pre-created (e.g. ViewPager tabs not yet visible)

---

## 2. Three Approaches Evaluated

### Approach A — `LaunchedEffect(Unit)` in Compose

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(MyIntent.LoadData)
    }
}
```

| | |
|---|---|
| **Pro** | Full control over when loading happens |
| **Pro** | Testable — call the intent directly in unit tests |
| **Con** | Defeats the purpose of ViewModels outliving config changes |
| **Con** | Recomposition can re-trigger the `LaunchedEffect` |
| **Con** | Loading is tied to the UI layer — ViewModel doesn't own the trigger |

**When to use:** UI-only one-shot effects (show a Snackbar on screen entry), or when nav args drive the load and the ViewModel guards against duplicate fetches.

---

### Approach B — `init` block in ViewModel

```kotlin
class MyViewModel : ViewModel() {
    init {
        viewModelScope.launch { loadData() }
    }
}
```

| | |
|---|---|
| **Pro** | Simple; data is always available immediately after ViewModel creation |
| **Con** | **No testability** — load fires before test setup code can run |
| **Con** | **Eager loading** — fires even when the screen is not yet visible (pre-created tabs, nav back-stack) |
| **Con** | No control over when data loads; impossible to insert setup code between init and load |

**When to use:** Never for network/disk calls. Acceptable only for synchronous in-memory state initialisation.

---

### Approach C — `onStart` + `stateIn` + `initialDataLoad` ✅ **TravelMonk Standard**

The key insight: tie the data load to the **subscription lifecycle** of the `StateFlow`, not to ViewModel creation or UI composition.

```kotlin
// BaseViewModel — data load fires when uiState gains its first collector
val uiState: StateFlow<S> by lazy {
    _uiState
        .onStart {
            viewModelScope.launch { initialDataLoad() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialState
        )
}

// open so only ViewModels that load data need to override it
open suspend fun initialDataLoad() {}
```

Each ViewModel overrides only when it needs initial data:

```kotlin
@HiltViewModel
class ExperienceViewModel @Inject constructor(
    private val getExperiencesUseCase: GetExperiencesUseCase
) : BaseViewModel<ExperienceState, ExperienceIntent, ExperienceEffect>() {

    override suspend fun initialDataLoad() {
        loadItems(ExperienceCategory.PACKAGES)  // fires when UI subscribes
    }
}
```

| | |
|---|---|
| **Pro** | Load fires only when UI is active (has a subscriber) |
| **Pro** | `WhileSubscribed(5_000)` — survives rotation without re-fetching |
| **Pro** | `open` not `abstract` — ViewModels without initial data don't need to override |
| **Pro** | `by lazy` — `onStart` block runs only on first collection, not every access to `uiState` |
| **Pro** | Testable — collect the flow in `runTest` to trigger the load at the controlled moment |
| **Con** | Slightly more complex `BaseViewModel` setup (one-time cost) |

---

## 3. Why `WhileSubscribed(5_000)` — The ANR Connection

The 5-second timeout matches the Android **ANR (Application Not Responding) deadline** — not an arbitrary value (credit: Ian Lake).

```
User rotates device → UI unsubscribes → 5s window → UI resubscribes
                                                ├── within 5s: reuse cached state, no reload
                                                └── after 5s:  onStart fires again, fresh load
```

This prevents a loading flash during rotation while still releasing upstream resources after the app is genuinely backgrounded for a meaningful time.

---

## 4. Decision Tree — Which Approach to Use

```
Does the data load depend on nav args passed to the screen?
├── YES → Use LaunchedEffect(key = navArg) { onIntent(Load(navArg)) }
│         ViewModel guards: if (currentState.data.isNotEmpty()) return
│         Example: FlightResultsScreen loads by (from, to) pair
│
└── NO  → Does the screen always need data when it becomes visible?
          ├── YES → Override initialDataLoad() in ViewModel  ← standard path
          │         Example: ExperienceViewModel, BookingViewModel, HomeViewModel
          │
          └── NO  → Don't auto-load; wait for an explicit user Intent
                    Example: FlightSearchScreen (user fills form, then taps Search)
                              StaySearchScreen  (user enters location, then taps Search)
```

---

## 5. TravelMonk Implementation Status

| ViewModel | Pattern Used | Notes |
|---|---|---|
| `ExperienceViewModel` | `initialDataLoad()` override | Loads default category on subscription |
| `BookingViewModel` | `initialDataLoad()` override | Loads bookings list on subscription |
| `HomeViewModel` | `initialDataLoad()` override | Loads banners on subscription |
| `FlightViewModel` (`LoadResults`) | `LaunchedEffect(from, to)` | Nav-arg driven — correct for this case |
| `FlightViewModel` (`SearchFlights`) | Intent-driven | User triggers search explicitly |
| `StayViewModel` | Intent-driven | User triggers search explicitly |

---

## 6. Rules for TravelMonk

1. **Never put a network/disk call in `init`** — use `initialDataLoad()` override instead
2. **Never use `LaunchedEffect(Unit)` for data the ViewModel should own** — use `initialDataLoad()`
3. **`LaunchedEffect(key)` is correct** when nav args drive the load (key = the nav arg value)
4. **Always add a guard** in ViewModel when `LaunchedEffect` triggers a load: `if (currentState.data.isNotEmpty()) return`
5. **`SharingStarted.WhileSubscribed(5_000)` is mandatory** — never use `Eagerly` or `Lazily` for screen-level state
6. **`open` not `abstract`** — ViewModels that only handle user intents should not be forced to override

---

## 7. Testing Implication

With `initialDataLoad()`, unit tests control when loading starts by controlling when they collect `uiState`:

```kotlin
@Test
fun `loadItems success updates state`() = runTest {
    // Arrange — configure fake BEFORE collection starts
    fakeRepo.setResult(DataResult.Success(fakeExperiences))

    // Act — collecting uiState triggers initialDataLoad()
    val state = viewModel.uiState.first { !it.isLoading }

    // Assert
    assertEquals(fakeExperiences, state.items)
}
```

With `init`, the load fires at ViewModel instantiation — before the fake is configured — making tests unreliable.
