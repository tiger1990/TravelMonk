# Architectural Standards & Enforcement

This project uses **Konsist** to automatically enforce architectural standards. These rules are verified during the unit test phase of the `:app` module.

## 1. Core Principles
- **Clean Architecture:** Separation of concerns between Data, Domain, and UI layers.
- **Unidirectional Data Flow (MVI):** State flows down, Intents flow up.
- **Feature Modularization:** Features are isolated and communicate through APIs.

## 2. Structural Rules

### ViewModels
- **Naming:** Must always end with the suffix `ViewModel` (e.g., `BookingViewModel`).
- **Location:** Must reside in a package ending with `.ui`.
- **Inheritance:** Must extend `BaseViewModel`.

### Repositories
- **Naming:** Interfaces must end with `Repository`. Implementations must end with `RepositoryImpl`.
- **Location:** 
    - Interfaces: `domain.repository`
    - Implementations: `data.repository`

### Use Cases
- **Naming:** Must end with `UseCase`.
- **Method:** Must have a single public method named `invoke`.

### Dependency Injection
- **Hilt Modules:** Should be `internal` and reside in a `.di` package.

---

## 3. Enforcement
The enforcement tests are located in:
`app/src/test/java/com/travelmonk/ArchitectureCheck.kt`

To run the checks locally:
```bash
./gradlew :app:testDebugUnitTest --tests "com.travelmonk.ArchitectureCheck"
```
