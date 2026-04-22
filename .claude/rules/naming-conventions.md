# Naming Conventions — TravelMonk

Consistent naming helps us navigate the codebase and understand the role of each file instantly.

## 1. MVI Components
- **Intents**: `[Feature]Intent` (e.g., `ExperiencesIntent`).
- **State**: `[Feature]State` (e.g., `ExperiencesState`).
- **Effects**: `[Feature]Effect` (e.g., `ExperiencesEffect`).
- **ViewModel**: `[Feature]ViewModel` (e.g., `ExperiencesViewModel`).

## 2. UI Components
- **Stateful Screens**: `[Feature]Screen.kt` (The entry point).
- **Stateless Content**: `[Feature]ScreenContent` (The main layout).
- **Specific Components**: `[Feature][Component]` (e.g., `ExperiencesTopBar`, `ExperienceCard`).

## 3. Package Structure
- `ui/`: Contains Screens and internal components.
- `domain/model/`: Contains pure data classes.
- `mvi/`: Contains State, Intent, and Effect definitions.
- `di/`: Contains Hilt modules.
