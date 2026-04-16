# TravelMonk — Task Backlog

---

## Done

### ✅ Wire Use Case Layer + Data API injection (no Room)

**Context:**
All `data/api/`, `domain/usecase/`, and `domain/model/` files were created but are not wired into DI or repositories.
Old `data/remote/*Api.kt` files were deleted; repositories now reference the new `data/api/` interfaces — but DI modules haven't been updated to provide them.

**Checklist:**
- [x] **flights/stays/experiences/home** — already fully wired (DI + repo + use case), no changes needed
- [x] **services** — added `services` field to `ServicesState`, fixed `loadServices()` to store result in state, updated `ServicesScreen` to use `state.services.ifEmpty { defaultServices }`
- [x] **bookings** — added `@Provides BookingsApi` to `BookingModule`
- [x] Compile check: `:feature:services` and `:feature:bookings` — BUILD SUCCESSFUL

**Notes:**
- `data/local/` placeholders exist but Room wiring is deferred (separate task below)
- All repositories have mock fallbacks — app will function even before real API is connected

---

## Done

### ✅ Section 3 — ViewModel & MVI gaps (G-13, G-19, G-20)

**G-14 removed** — `LaunchedEffect(Unit)` for effect collection is the project-endorsed pattern per `compose-ui-best-practice.md`

**Checklist:**
- [ ] **G-13** — Replace `collectAsState()` with `collectAsStateWithLifecycle()` in 7 screens:
  - [ ] `FlightSearchScreen.kt`
  - [ ] `StaySearchScreen.kt`
  - [ ] `HomeScreen.kt`
  - [ ] `ExperiencesScreen.kt`
  - [ ] `ServicesScreen.kt`
  - [ ] `MyBookingsScreen.kt`
  - [ ] `TransportScreen.kt`
- [ ] **G-19** — Fix `FlightViewModel` race conditions:
  - `SwapCities`: use `this.toCode`/`this.fromCode` inside `setState` lambda (not `currentState.*`)
  - `SearchFlights`: capture `fromCity`/`toCity` snapshot before launching coroutine
- [ ] **G-20** — Add `withContext(Dispatchers.IO)` to all 5 repository suspend functions:
  - [ ] `FlightRepositoryImpl`
  - [ ] `StayRepositoryImpl`
  - [ ] `ExperienceRepositoryImpl`
  - [ ] `HomeRepositoryImpl`
  - [ ] `ServiceRepositoryImpl`
- [ ] Update `ArchitectureGaps.md` — remove fixed Section 3 items, update tracker + summary

---

## Pending

### [ ] Wire `data/local/` — Room Integration for all feature modules

**Context:**
Each feature module has a `data/local/*LocalDataSource.kt` placeholder file with a commented-out Room DAO skeleton. The schema contract is already defined. This task wires them up once `core:database` gets Room integrated.

**Scope:**
- `feature/flights/data/local/FlightLocalDataSource.kt` → implement `FlightDao` (cache flight search results by from/to)
- `feature/stays/data/local/StayLocalDataSource.kt` → implement `StayDao` (cache stays by location)
- `feature/experiences/data/local/ExperienceLocalDataSource.kt` → implement `ExperienceDao` (cache experiences by category)
- `feature/bookings/data/local/BookingLocalDataSource.kt` → implement `BookingDao` (persist bookings, support cancel via status update)
- `feature/services/data/local/ServiceLocalDataSource.kt` → implement `ServiceDao` (cache services list)
- `feature/home/data/local/HomeLocalDataSource.kt` → implement `HomeBannerDao` (cache home banners)

**Prerequisites:**
- `core:database` module needs Room dependency + `AppDatabase` class
- Each feature's `*RepositoryImpl.kt` must be updated to coordinate `api/` (network) + `local/` (cache) — fetch from network, write to local, read from local on failure

**Related:**
- ArchitectureGaps.md G-25: `core:database` declared but empty
- ArchitectureGaps.md G-30: API DTOs decoupled from domain models (introduce `data/api/dto/` + mappers at the same time)

---
