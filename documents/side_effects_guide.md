# Compose Side Effects — TravelMonk

Reference guide for Jetpack Compose effect APIs used in this project.
See also: `.claude/rules/compose-ui-best-practice.md` § 4 for the decision table.

---

## Quick Reference

| Situation                                          | API                      | Key rule                              |
|----------------------------------------------------|--------------------------|---------------------------------------|
| One-time or key-triggered async work               | `LaunchedEffect(key)`    | Re-runs when key changes              |
| Cleanup needed on leave / key change               | `DisposableEffect(key)`  | Always provide `onDispose {}`         |
| Sync non-Compose side effect after recomposition   | `SideEffect`             | Runs after every successful recompose |
| User-triggered coroutine (button click)            | `rememberCoroutineScope()` | Scope tied to composition           |
| Derived / computed value from state                | `derivedStateOf { }`     | Avoids excess recomposition           |
| Expensive object surviving recomposition           | `remember { }`           | Created once per composition          |

---

## DisposableEffect

### When to use

Use `DisposableEffect` when you need to **register something and must clean it up** when
the composable leaves composition or when the key changes.

Common use cases:
- Register / unregister listeners
- Subscribe / unsubscribe to streams
- Add / remove observers
- Attach / detach callbacks
- Work with external APIs that require explicit teardown

### How it works

```
key changes        →  onDispose {} runs  →  effect block re-runs (registers again)
composition leaves →  onDispose {} runs  (final cleanup)
```

### Lifecycle observer example

```kotlin
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        // handle lifecycle events
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
    }
}
```

The key `lifecycleOwner` means: whenever the `lifecycleOwner` reference changes,
dispose the old observer and re-register with the new one.

---

## TravelMonk Usage — NavigationBus Binding

The `globalNavigator` must be bound to the current `NavigationState` so it knows
which tab stack to dispatch commands to. `DisposableEffect` is the correct API here
because the binding has a symmetric teardown (`unbind`).

```kotlin
// TravelMonkApp.kt (or NavHost entry point)
DisposableEffect(navigationState) {
    globalNavigator.bind(navigationState)
    onDispose { globalNavigator.unbind() }
}
```

**Why `DisposableEffect` and not `LaunchedEffect`:**
- `globalNavigator.bind()` is not a suspend call — no coroutine needed
- There is an explicit cleanup (`unbind`) that must run on navigation state change
- `LaunchedEffect` has no `onDispose` hook — wrong tool for symmetric register/unregister

**Why `navigationState` as the key:**
Whenever `navigationState` changes (user switches tabs, back stack updates), the effect
re-runs: the old binding is disposed and a fresh binding is registered against the new state.

---

## LaunchedEffect — comparison

Use `LaunchedEffect` when the work is async (suspend) and cleanup is implicit via coroutine
cancellation:

```kotlin
// Load data when screen enters
LaunchedEffect(Unit) {
    viewModel.onIntent(FlightIntent.Load)
}

// Collect one-shot effects (navigation, snackbar)
LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
        when (effect) {
            is FlightEffect.NavigateToDetail -> navigator.openDetail(effect.id)
        }
    }
}
```

When the key changes or the composable leaves, the coroutine is **cancelled automatically** —
no manual cleanup needed, which is why `LaunchedEffect` has no `onDispose`.

---

## Common Mistakes

| Mistake                                               | Problem                         | Fix                          |
|-------------------------------------------------------|---------------------------------|------------------------------|
| `LaunchedEffect` for register/unregister              | No cleanup hook — listener leaks | Use `DisposableEffect`      |
| `DisposableEffect` for suspend work                   | Not a coroutine scope           | Use `LaunchedEffect`         |
| Key = `Unit` on `DisposableEffect` with changing deps | Effect never re-runs            | Pass the actual dep as key   |
| Empty or missing `onDispose {}`                       | Resource leak / crash           | Always implement `onDispose` |
| `GlobalScope.launch` in composable body               | Not tied to composition lifecycle | Use `rememberCoroutineScope()` |
