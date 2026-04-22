# Jetpack Compose Best Practices — TravelMonk Skill Sheet

This document serves as a quick-reference guide for building premium, maintainable, and testable UI in the TravelMonk project.

---

## 1. Architectural Pattern: Stateful → Stateless
Always split a screen into two distinct layers to ensure testability and high-quality previews.

- **`*Screen` (Stateful)**: 
    - The "Glue" layer.
    - Connects to ViewModel via `hiltViewModel()`.
    - Collects state using `collectAsStateWithLifecycle()`.
    - Handles navigation and side effects (Toasts, SnackBar) via `LaunchedEffect`.
    - **Rule**: Never add `@Preview` here.

- **`*Content` (Stateless)**:
    - The "Pure UI" layer.
    - Accepts a single `State` object and an `onIntent` lambda.
    - Contains the `Scaffold` and structural layout.
    - **Rule**: This is the primary target for `@Preview` (Light & Dark).

---

## 2. Unidirectional Data Flow (UDF)
- **State flows down**: Pass the entire UI state object into the content composable.
- **Events flow up**: Use a single `onIntent: (Intent) -> Unit` lambda to communicate user actions back to the ViewModel.
- **Benefit**: Predictable UI logic and easy debugging.

---

## 3. Atomic Decomposition (The "TopBar" Rule)
Don't let your `Scaffold` get cluttered. If a component (like a TopBar or Header) has internal logic (e.g., TabRows, Search inputs), extract it into its own private composable.

- **Readability**: Keeps the main `Scaffold` layout acting as a "map" of the screen.
- **Isolated Previews**: Allows you to preview just the header or complex component without rendering the whole screen.
- **Maintenance**: Makes it easier to swap or update specific UI regions.

---

## 4. State Hoisting & Pure Functions
- Move state to the **lowest common parent** that needs it.
- Ensure children are "dumb" — they should only know how to render what they are given and report clicks.

---

## 5. Modifier Passing (Standard Practice)
Every public composable should accept a `modifier: Modifier = Modifier` as its first optional parameter.
- **Rule**: Always apply this modifier to the root element of the composable.
- **Reason**: Allows the parent to control layout constraints (padding, weight, size) without the child needing to know about its context.

---

## 6. Zero Hardcoding (Semantic Theming)
- **Colors**: Use `TravelMonkTheme.colors.*` (never `Color.White` or hex codes).
- **Typography**: Use `TravelMonkTheme.typography.*`.
- **Dimensions**: Use `TravelMonkTheme.spacing.*` or `TravelMonkTheme.dimensions.*`.
- **Reason**: Ensures that Dark Mode and theme-switching (like the Glass UI) work automatically.

---

## 7. Meaningful Previews
Every `*Content` composable must have:
1. **Light Theme Preview** (`showSystemUi = true`).
2. **Dark Theme Preview** (`uiMode = NIGHT_YES`).
3. **Realistic Mock Data**: Don't just preview empty states; use representative data to catch layout issues early.

---

## 8. Data Transformation Rule
- **Never** perform `.map()`, `.filter()`, or `.sorted()` directly in a Composable body.
- **Do it in the ViewModel**: Emit ready-to-render domain models.
- **If unavoidable**: Wrap the transformation in `remember(key) { ... }`.
