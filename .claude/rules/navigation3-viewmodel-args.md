# Navigation3 + ViewModel Arguments Rule — TravelMonk

## The Problem

With **Navigation Component** (legacy), arguments are passed via the nav graph and automatically
populated into `SavedStateHandle`. This pattern does NOT work with **Navigation3**.

With **Navigation3**, typed nav keys carry data directly (e.g. `OnboardingNavKey.Otp(val phone: String)`),
but `SavedStateHandle` is never auto-populated from those properties.

**Never do this with Navigation3:**
```kotlin
// CRASH — savedStateHandle["phone"] is always null in Navigation3
@HiltViewModel
class OtpViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<...>() {
    private val phone: String = checkNotNull(savedStateHandle["phone"]) // NPE!
}
```

## The Rule: Use Hilt Assisted Injection for Nav Key Arguments

When a ViewModel needs data from a typed nav key, use `@AssistedInject` + `@AssistedFactory`.
Hilt generates the factory automatically — no manual DI binding needed.

### ViewModel

```kotlin
@HiltViewModel(assistedFactory = FooViewModel.Factory::class)
class FooViewModel @AssistedInject constructor(
    @Assisted val navArg: String,                    // comes from nav key
    private val savedStateHandle: SavedStateHandle,  // still OK for process-death persistence
    private val someUseCase: SomeUseCase,
) : BaseViewModel<FooState, FooIntent, FooEffect>() {

    @AssistedFactory
    interface Factory {
        fun create(navArg: String): FooViewModel
    }

    override fun createInitialState() = FooState(navArg = navArg)
}
```

### Screen Composable

```kotlin
@Composable
fun FooScreen(
    navigator: FooNavigator,
    navArg: String,                        // received from the nav host entry
    viewModel: FooViewModel = hiltViewModel<FooViewModel, FooViewModel.Factory> { factory ->
        factory.create(navArg)
    }
) { ... }
```

### Nav Host Entry (Navigation3)

```kotlin
entry<FooNavKey> { key ->
    FooScreen(navigator = navigator, navArg = key.navArg)
}
```

## When savedStateHandle Is Still Correct

`SavedStateHandle` remains the right tool for **process-death persistence** (cooldown timers,
scroll position, transient UI state). It must NOT be used to receive nav arguments in Navigation3.

| Use Case | Correct Approach |
|----------|-----------------|
| Nav arguments (e.g. phone, id) | `@Assisted` via `@AssistedInject` |
| Process-death persistence (e.g. cooldown timer) | `savedStateHandle.get<T>(key)` |
| Deep-link arguments | `savedStateHandle` (populated by the system) |