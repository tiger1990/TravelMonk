# Architectural Rules — TravelMonk

These rules maintain the integrity of our multi-module, MVI-based architecture.

## 1. Module Boundaries
- **Feature Isolation**: `:feature:X` modules must **never** depend on other `:feature:Y` modules. They must communicate via `:feature:Y:api` modules.
- **Data Flow**: The UI layer must only interact with `Domain` models. `Data` layer models (DTOs) must be mapped to Domain models before reaching the ViewModel.
- **Dependency Direction**: `Data` → `Domain` ← `UI`. 

## 2. MVI Pattern (Mandatory)
Every screen must follow the `Intent` -> `State` -> `Effect` pattern.
- **State**: A single `@Immutable` data class.
- **Intent**: A sealed interface representing user actions.
- **Effect**: A sealed interface for one-time side effects (navigation, snackbars).
- **Collection**: State must be collected using `collectAsStateWithLifecycle()`.

## 3. Dependency Injection
- Always use **Hilt** for dependency injection.
- Feature-to-feature navigation handlers must be provided via Hilt modules in the feature implementation.
