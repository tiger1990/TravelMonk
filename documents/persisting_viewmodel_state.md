# Persisting ViewModel State in Navigation3

## Overview

This document covers two concerns:

1. **NavEntry-scoped ViewModels** — ensuring ViewModels survive process death by binding their lifetime to a `NavEntry` via `rememberViewModelStoreNavEntryDecorator`
2. **UI state modelling** — structuring `StateFlow`-based state with proper loading / success / error handling

---

## 1. NavEntry-Scoped ViewModels (Process Death Survival)

For feature ViewModels to survive process death, they must be scoped to the `NavEntry` — not the Activity or the entire back stack. This is done by adding `rememberViewModelStoreNavEntryDecorator` to the `entryDecorators` list in `NavDisplay`.

```kotlin
NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators = listOf(
        // CRITICAL: scopes each ViewModel to its NavEntry.
        // Enables SavedStateHandle and survives process death.
        rememberViewModelStoreNavEntryDecorator()
    ),
    entryProvider = myEntryProvider
)
```

> **Why:** Without this decorator, ViewModels are scoped to the Activity and share a single `ViewModelStore`. With it, each destination gets its own store — matching the lifecycle of that screen in the back stack.

---

## 2. Accessing Nav Args in a Feature ViewModel

Navigation3 automatically populates `SavedStateHandle` with the serialized properties of the destination's `NavKey` data class. No manual argument extraction is needed.

```kotlin
// NavKey definition (in feature-api module)
@Serializable
@SerialName("booking.confirmation")
data class Confirmation(val type: BookingType, val title: String) : BookingNavKey

// ViewModel reads args directly from SavedStateHandle
@HiltViewModel
class ConfirmationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Nav3 populates the handle with the fields from the Confirmation data class
    val title: String = checkNotNull(savedStateHandle["title"])
    val type: BookingType = checkNotNull(savedStateHandle["type"])

    // UI state that also survives process death via SavedStateHandle
    var isAgreed by savedStateHandle.saveable { mutableStateOf(false) }
}
```

> **Why:** `SavedStateHandle` is backed by `onSaveInstanceState`, so both nav args and local UI state survive the OS killing the process. `checkNotNull` surfaces missing args immediately rather than causing a null crash deeper in the code.

---

## 3. ViewModel + UI State Model Pattern

### Define a Sealed UI State

```kotlin
sealed class UiState {
    data object Idle    : UiState()
    data object Loading : UiState()
    data class  Success(val data: List<String>) : UiState()
    data class  Error(val message: String)      : UiState()
}
```

> **Why sealed:** The `when` expression in the Composable becomes exhaustive — the compiler enforces that every state is handled. No `else ->` branch needed (except for truly unhandled default cases).

---

### ViewModel — Reactive Pattern with `stateIn`

```kotlin
class MyViewModel : ViewModel() {

    private val trigger = MutableSharedFlow<Unit>()

    // StateFlow derived from the trigger — reacts to explicit fetch requests.
    // WhileSubscribed(5000) keeps the flow alive for 5 s after the last observer
    // disappears (e.g. during screen rotation) to avoid unnecessary re-fetches.
    val uiState: StateFlow<UiState> = trigger
        .onStart { emit(Unit) }               // load immediately on first subscription
        .flatMapLatest {
            flow {
                emit(UiState.Loading)
                emit(UiState.Success(fetchData()))
            }.catch { e ->
                emit(UiState.Error("Something went wrong"))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Idle
        )

    fun refresh() {
        viewModelScope.launch { trigger.emit(Unit) }
    }
}
```

> **Why `stateIn` + `WhileSubscribed(5000)`:** Preferred over a raw `MutableStateFlow` because the upstream flow is only active while the UI is subscribed. The 5-second grace period survives configuration changes without re-triggering the network call.

---

### ViewModel — Imperative Pattern (simpler alternative)

```kotlin
class MyViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    init { fetchData() }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _uiState.value = UiState.Success(fetchData())
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load data")
            }
        }
    }

    fun retry() = fetchData()
}
```

> **When to use:** For simple screens with a single data source and no complex retry/cancel logic. Easier to read; less powerful than the reactive pattern above.

---

### Composable — Clean State-Driven UI

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {

    // collectAsStateWithLifecycle stops collection when the screen is in the background,
    // preventing unnecessary UI updates and resource waste.
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column {
        Button(onClick = { viewModel.refresh() }) {
            Text("Fetch Data")
        }

        when (state) {
            is UiState.Idle    -> { /* no-op */ }
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                LazyColumn {
                    items((state as UiState.Success).data) { item ->
                        Text(item)
                    }
                }
            }
            is UiState.Error   -> {
                val message = (state as UiState.Error).message
                Column {
                    Text("Error: $message")
                    Button(onClick = { viewModel.retry() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
```

> **Why `collectAsStateWithLifecycle`:** Unlike `collectAsState()`, this suspends collection when the lifecycle drops below `STARTED` (app in background), matching Android's recommended lifecycle-aware approach.

---

## 4. Granular Error Handling

Catch specific exception types to give users actionable messages:

```kotlin
try {
    _uiState.value = UiState.Success(fetchData())
} catch (e: IOException) {
    _uiState.value = UiState.Error("No internet connection")
} catch (e: HttpException) {
    _uiState.value = UiState.Error("Server error (${e.code()})")
} catch (e: Exception) {
    _uiState.value = UiState.Error("Unexpected error")
}
```

> **Why:** A generic `catch (e: Exception)` swallows all failures silently. Specific catches let the UI display the right message and let the user take the right action (e.g. retry vs. check connection).

---

## 5. Paging 3 (for large lists)

When a list is backed by a paginated API, use Paging 3 instead of loading everything into a `List<T>`.

### ViewModel

```kotlin
class MyViewModel : ViewModel() {

    // Pager is created once. cachedIn keeps loaded pages alive across
    // recompositions and configuration changes — no re-fetch on rotation.
    val pager: Flow<PagingData<String>> = Pager(
        config = PagingConfig(pageSize = 20)
    ) {
        MyPagingSource()
    }.flow.cachedIn(viewModelScope)
}
```

> **Why `cachedIn(viewModelScope)`:** Without it, every new collector (e.g. after rotation) restarts the paging stream from page 1. `cachedIn` replays already-loaded pages instantly and only fetches new ones.

---

### Composable

```kotlin
@Composable
fun MyPagingScreen(viewModel: MyViewModel = viewModel()) {

    // collectAsLazyPagingItems integrates PagingData with LazyColumn.
    // It handles load states (loading, error, endOfPaginationReached) automatically.
    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()

    LazyColumn {
        items(lazyPagingItems.itemCount) { index ->
            val item = lazyPagingItems[index]
            item?.let { Text(it) }
        }

        // Optional: append state footer
        lazyPagingItems.apply {
            when {
                loadState.append is LoadState.Loading ->
                    item { CircularProgressIndicator() }
                loadState.append is LoadState.Error ->
                    item { Button(onClick = { retry() }) { Text("Retry") } }
            }
        }
    }
}
```

> **Why `collectAsLazyPagingItems()`:** Unlike `collectAsStateWithLifecycle()` on a plain list, this operator understands paging load states and triggers page loads as the user scrolls — no manual offset/limit management required.

---

## Summary

| Concern | Mechanism | Key API |
|---|---|---|
| ViewModel scoped to destination | Entry decorator | `rememberViewModelStoreNavEntryDecorator()` |
| Nav args in ViewModel | Automatic handle population | `SavedStateHandle["argName"]` |
| UI state survives process death | SavedStateHandle saveable | `savedStateHandle.saveable { }` |
| Lifecycle-aware state collection | Lifecycle-aware operator | `collectAsStateWithLifecycle()` |
| Reactive state stream | Cold flow → hot StateFlow | `stateIn(WhileSubscribed(5000))` |
| Granular error messages | Typed catch blocks | `IOException`, `HttpException` |
| Paginated lists | Paging 3 + PagingSource | `Pager`, `cachedIn`, `collectAsLazyPagingItems()` |
