# Compose UI Best Practices — TravelMonk

These rules apply to every Composable written or modified in this project.
Think in **State → UI**, never UI → State.

---

## 1. Unidirectional Data Flow (UDF) — Non-Negotiable

Every screen follows this contract:

```
ViewModel (StateFlow<State>) → Composable renders State
Composable → sends Intent/Event → ViewModel processes → emits new State
Side effects → Channel<Effect> → collected once in the UI layer
```

- **Never** read from a repository, database, or SharedPreferences inside a Composable
- **Never** trigger business logic directly from a click handler — send an Intent to the ViewModel
- **Always** collect state with `collectAsStateWithLifecycle()`, never `collectAsState()`

```kotlin
// ✅ Correct
val state by viewModel.state.collectAsStateWithLifecycle()

// ❌ Wrong — ignores lifecycle, leaks in background
val state by viewModel.state.collectAsState()
```

---

## 2. Stateless Composables by Default

Every screen is split into two composables:

```kotlin
// Stateful — owns the ViewModel connection, not previewable alone
@Composable
fun BookingScreen(viewModel: BookingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect -> /* handle navigation/toast */ }
    }
    BookingContent(state = state, onIntent = viewModel::onIntent)
}

// Stateless — pure state-to-UI mapping, fully previewable & testable
@Composable
fun BookingContent(
    state: BookingState,
    onIntent: (BookingIntent) -> Unit,
    modifier: Modifier = Modifier
) { ... }
```

- `*Screen` composables: stateful, connect to ViewModel, not previewed directly
- `*Content` composables: stateless, take `state` + `onIntent`, always previewed

---

## 3. State Hoisting — The Golden Rule

Move state up to the **lowest common parent** that needs it.

```kotlin
// ❌ State trapped inside — untestable, non-reusable
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }
    TextField(value = query, onValueChange = { query = it })
}

// ✅ State hoisted — caller controls it
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(value = query, onValueChange = onQueryChange)
}
```

---

## 4. Effect APIs — Use the Right One

| Situation | API |
|-----------|-----|
| One-time or key-triggered async work (load data, observe events) | `LaunchedEffect(key)` |
| Cleanup needed on leave (listeners, subscriptions) | `DisposableEffect(key)` |
| Sync non-Compose side effect after every recomposition | `SideEffect` |
| User-triggered coroutine (button click) | `rememberCoroutineScope()` |
| Derived/computed value from state | `derivedStateOf { }` |
| Expensive object that survives recomposition | `remember { }` |

```kotlin
// ✅ Load on screen entry, cancel on exit
LaunchedEffect(Unit) {
    viewModel.onIntent(FlightIntent.Load)
}

// ✅ Collect one-shot effects (navigation, snackbar)
LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
        when (effect) {
            is FlightEffect.NavigateToDetail -> navigator.openDetail(effect.id)
        }
    }
}

// ✅ Derived state — avoids recomposition when source hasn't meaningfully changed
val isButtonEnabled by remember {
    derivedStateOf { state.query.length >= 3 && !state.isLoading }
}

// ❌ Never do heavy work inline — blocks the composition thread
val data = repository.loadSync() // WRONG
```

---

## 5. Stability — Design for Smart Recomposition

Compose skips recomposition only if inputs are **stable**. Unstable inputs = unnecessary redraws = jank.

### Rules:
- Use `data class` with only immutable (`val`) properties for all state passed to Composables
- Annotate with `@Immutable` when all properties are deeply immutable
- Annotate with `@Stable` when Compose cannot infer stability (e.g. interface types)
- **Never** pass mutable collections (`List`, `Map`) directly — use `ImmutableList` (kotlinx.collections.immutable) or `@Immutable` wrappers
- **Never** pass lambdas that capture mutable state without `remember`

```kotlin
// ✅ Stable state
@Immutable
data class FlightState(
    val flights: ImmutableList<Flight> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ✅ Stable lambda
val onClick = remember(id) { { viewModel.onIntent(Select(id)) } }

// ❌ Unstable — triggers recomposition every time
FlightCard(flights = listOf(...)) // new list instance every recomposition
```

---

## 6. Dumb Composables — No Data Transformation Inside UI

Composables should **only render state** — they must not transform or compute derived data inline.

Any `.map()`, `.filter()`, `.sorted()`, or similar collection operation performed directly inside a Composable body (outside of `remember`) re-executes on **every recomposition** — including those triggered by animations, scroll, or unrelated state changes.

```kotlin
// ❌ Anti-pattern — remaps the entire list every recomposition
@Composable
fun FlightList(flights: List<FlightDto>) {
    val items = flights.map { it.toDomain() } // runs on EVERY recomposition
    LazyColumn {
        items(items) { FlightCard(it) }
    }
}

// ✅ Acceptable — wrap in remember so mapping only runs when `flights` changes
@Composable
fun FlightList(flights: List<FlightDto>) {
    val items = remember(flights) { flights.map { it.toDomain() } }
    LazyColumn {
        items(items, key = { it.id }) { FlightCard(it) }
    }
}

// ✅ Preferred — transformation belongs in the ViewModel, not the UI
// ViewModel emits ImmutableList<Flight> (already mapped domain models) via StateFlow
// Composable receives ready-to-render state — zero transformation needed
@Composable
fun FlightList(flights: ImmutableList<Flight>) {
    LazyColumn {
        items(flights, key = { it.id }) { FlightCard(it) }
    }
}
```

**Rules:**
- **Never** call `.map()`, `.filter()`, `.sorted()`, `.groupBy()`, or any collection transform directly in a Composable body
- If transformation is unavoidable in the UI layer, wrap it in `remember(dependency) { ... }`
- **Prefer** moving all transformations to the ViewModel — emit ready-to-render state
- The ViewModel is the right place for data shaping; the Composable is only responsible for rendering

---

## 7. `remember` and `key` Usage

```kotlin
// ✅ Expensive object — created once
val formatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

// ✅ Key changes → effect re-runs
LaunchedEffect(userId) { viewModel.onIntent(Load(userId)) }

// ✅ Key in lazy lists — stable identity prevents item recreation
LazyColumn {
    items(flights, key = { it.id }) { flight ->
        FlightCard(flight = flight)
    }
}

// ❌ No key in lazy list — Compose cannot track identity
items(flights) { FlightCard(it) }
```

---

## 8. Lifecycle-Safe Patterns

```kotlin
// ✅ collectAsStateWithLifecycle — stops collection when UI is not visible
val state by viewModel.state.collectAsStateWithLifecycle()

// ✅ rememberUpdatedState — safe reference to latest lambda in long-lived effects
val currentOnTimeout by rememberUpdatedState(onTimeout)
LaunchedEffect(Unit) {
    delay(3000)
    currentOnTimeout() // always calls latest lambda, not a stale capture
}

// ❌ Never start coroutines in Composable body
GlobalScope.launch { ... } // WRONG — not tied to composition lifecycle
```

---

## 9. Theming & Design System — Zero Hardcoding

```kotlin
// ✅ Always use design system tokens
Text(
    text = stringResource(R.string.book_now),
    style = TravelMonkTheme.typography.titleMedium,
    color = TravelMonkTheme.colorScheme.primary
)

// ❌ Never hardcode
Text(text = "Book Now", fontSize = 16.sp, color = Color(0xFF1A73E8))
```

- Colors → `TravelMonkTheme.colorScheme.*`
- Typography → `TravelMonkTheme.typography.*`
- Spacing/padding → design system spacing tokens
- Shapes → `TravelMonkTheme.shapes.*`
- Strings → `stringResource(R.string.*)` — never inline literals

---

## 10. Previews — Mandatory for Every Composable

Every `*Content` composable must have both Light and Dark previews.

```kotlin
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BookingContentPreview() {
    TravelMonkTheme {
        BookingContent(
            state = BookingState(/* realistic fake data */),
            onIntent = {}
        )
    }
}
```

- **Never** skip the Dark preview
- Use realistic fake data — not empty states only
- Cover all meaningful states: Loading, Success, Empty, Error

---

## 11. Pre-Commit Composable Checklist

Before marking any Composable done, verify:

- [ ] No business logic or I/O in the Composable body
- [ ] No `.map()` / `.filter()` / `.sorted()` called directly in Composable body — use ViewModel or `remember`
- [ ] Expensive computations wrapped in `remember { }`
- [ ] Computed/derived values use `derivedStateOf { }`
- [ ] Lazy lists have `key = { item.id }` on every `items()` call
- [ ] No mutable collections passed as params — use `ImmutableList`
- [ ] Lambdas that cause instability wrapped in `remember`
- [ ] `collectAsStateWithLifecycle()` used — not `collectAsState()`
- [ ] Both Light + Dark `@Preview` present on every `*Content` composable
- [ ] No hardcoded colors, strings, or dimensions
- [ ] State is hoisted to the appropriate level
- [ ] Side effects use the correct Effect API for the use case
