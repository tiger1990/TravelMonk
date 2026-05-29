# TravelMonk — Local DB / DataResult Refactor Plan

> Reviewed by: Principal Architect (2026-05-28)
> Status legend: `[ ]` = todo · `[~]` = in progress · `[x]` = done

---

## Overview

Two gaps from `tasks/gaps.md` (G10, G11) are solved together in 3 sequential PRs.

**Core principle (NowInAndroid / Google):**
- `DataResult.Loading` is a **UI concern**, not a data-layer concern.
- A `suspend` function IS loading while suspended — emitting `Loading` from `suspend` is structurally impossible and misleading.
- `Loading` is only semantically correct as the **first emission of a reactive `Flow`** (e.g. a Room DAO query).
- The compiler enforces the contract after this refactor.

**Final architecture after all 3 PRs:**

| Result type | Used for | Valid states |
|---|---|---|
| `kotlin.Result<T>` | All `suspend` mutations | `Success`, `Failure` |
| `Flow<DataResult<T>>` | All reactive queries (Room DAOs) | `Loading`, `Success`, `Error` |

`DataResult.Loading` is **reachable and correct** everywhere it survives — only on `Flow` streams, never from `suspend` calls.

---

## PR1 — G10: Pure mutations → `kotlin.Result<T>`

**Scope:** 5 use cases that are suspend mutations. Zero Room infrastructure needed.
Do NOT touch `SearchFlightsUseCase` / `SearchStaysUseCase` — they move to Flow in PR2.

### New file

- [ ] `core/common/.../result/SuspendRunCatching.kt` — safe coroutine-cancellation-aware helper

```kotlin
inline fun <T> suspendRunCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c          // must propagate — structured concurrency depends on it
    } catch (t: Throwable) {
        Result.failure(t)
    }
```

### Files to update

- [ ] `feature/onboarding/domain/repository/AuthRepository.kt` — `sendOtp` / `verifyOtp` return `Result<T>`
- [ ] `feature/onboarding/data/repository/AuthRepositoryImpl.kt` — use `suspendRunCatching`
- [ ] `feature/onboarding/domain/usecase/SendOtpUseCase.kt` — return `Result<Unit>`
- [ ] `feature/onboarding/domain/usecase/VerifyOtpUseCase.kt` — return `Result<AuthToken>`
- [ ] `feature/onboarding/domain/repository/PasskeyRepository.kt` — `auth` / `register` return `Result<T>`
- [ ] `feature/onboarding/data/repository/PasskeyRepositoryImpl.kt` — use `suspendRunCatching`
- [ ] `feature/onboarding/domain/usecase/PasskeyAuthUseCase.kt` — return `Result<Unit>`
- [ ] `feature/onboarding/domain/usecase/PasskeyRegistrationUseCase.kt` — return `Result<Unit>`
- [ ] `feature/bookings/domain/repository/BookingRepository.kt` — `cancelBooking` returns `Result<Unit>`
- [ ] `feature/bookings/data/repository/BookingRepositoryImpl.kt` — use `suspendRunCatching`
- [ ] `feature/bookings/domain/usecase/CancelBookingUseCase.kt` — return `Result<Unit>`
- [ ] `feature/onboarding/ui/OtpViewModel.kt` — replace dead `Loading` branch with `Result.onSuccess/onFailure`
- [ ] `feature/onboarding/ui/PasskeyPromptViewModel.kt` — same
- [ ] `feature/onboarding/ui/PhoneEntryViewModel.kt` — same
- [ ] `feature/bookings/ui/BookingViewModel.kt` — `cancelBooking` branch uses `Result`

### ViewModel pattern after PR1

```kotlin
// Before (dead Loading branch — unreachable from a suspend call)
when (val result = verifyOtpUseCase(phone, otp)) {
    is DataResult.Success -> { ... }
    is DataResult.Error   -> { ... }
    is DataResult.Loading -> Unit   // impossible — suspend always returns terminal value
}

// After (no Loading branch possible — compiler enforces it)
setState { copy(isLoading = true, error = null) }
verifyOtpUseCase(phone, otp)
    .onSuccess { setState { copy(isLoading = false) }; setEffect(OtpEffect.NavigateToPasskeyPrompt) }
    .onFailure { setState { copy(isLoading = false, error = it.message) } }
```

### Verification — PR1

- [ ] `grep -rn "DataResult.Loading" feature/onboarding feature/bookings --include="*.kt"` → zero hits in ViewModel or use case files
- [ ] `./gradlew :feature:onboarding:compileDebugKotlin :feature:bookings:compileDebugKotlin`
- [ ] Existing `OtpViewModelTest`, `PasskeyPromptViewModelTest`, `PhoneEntryViewModelTest` still pass
- [ ] Add test: fake repo returning `Result.failure(RuntimeException("bad otp"))` → assert error state emitted

---

## PR2 — G11 (core + Flights & Stays): Room infra + search → `Flow<List<T>>`

**Scope:** Core Room scaffolding in `core/database`, split Flights and Stays search into `observe* + refresh*`,
create dedicated `FlightResultsViewModel` and `StayResultsViewModel`.

> **Note:** `FlightResultsScreen.kt` currently injects `FlightViewModel` (the search screen's VM) — wrong coupling.
> This PR creates a dedicated results ViewModel and updates both screens.

### `core/common` — new file

- [ ] `core/common/.../result/DataResultExt.kt` — `asDataResult()` Flow extension

```kotlin
fun <T> Flow<T>.asDataResult(): Flow<DataResult<T>> =
    map<T, DataResult<T>> { DataResult.Success(it) }
        .onStart { emit(DataResult.Loading) }
        .catch   { emit(DataResult.Error(it, it.message)) }
```

### `core/database` — new files

- [ ] `core/database/.../entity/FlightEntity.kt`
- [ ] `core/database/.../entity/StayEntity.kt`
- [ ] `core/database/.../dao/FlightDao.kt` — `observeFlights(from, to): Flow<List<FlightEntity>>` + `@Upsert`
- [ ] `core/database/.../dao/StayDao.kt` — `observeStays(location): Flow<List<StayEntity>>` + `@Upsert`
- [ ] `core/database/.../TravelMonkDatabase.kt` — registers `FlightEntity`, `StayEntity` (extended in PR3)
- [ ] `core/database/.../di/DatabaseModule.kt` — provides `FlightDao`, `StayDao`

### Entity + DAO blueprint (Flights as template)

```kotlin
@Entity(tableName = "flights", primaryKeys = ["id", "fromCity", "toCity"])
data class FlightEntity(
    val id: String, val airline: String, val departureTime: String,
    val arrivalTime: String, val duration: String, val price: String,
    val fromCode: String, val toCode: String,
    val fromCity: String, val toCity: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Dao
interface FlightDao {
    @Query("SELECT * FROM flights WHERE fromCity = :from AND toCity = :to ORDER BY price ASC")
    fun observeFlights(from: String, to: String): Flow<List<FlightEntity>>

    @Upsert  // Room 2.5+ — insert or update atomically; triggers Flow re-emission
    suspend fun upsertFlights(flights: List<FlightEntity>)
}
```

### Repository interface split

```kotlin
interface FlightRepository {
    fun observeFlights(from: String, to: String): Flow<List<Flight>>    // reactive read
    suspend fun refreshFlights(from: String, to: String): Result<Unit>  // one-shot cache write
}
```

### Repository implementation pattern

```kotlin
class FlightRepositoryImpl @Inject constructor(
    private val api: FlightsApi,
    private val dao: FlightDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : FlightRepository {

    override fun observeFlights(from: String, to: String): Flow<List<Flight>> =
        dao.observeFlights(from, to)
            .map { it.map(FlightEntity::toDomain) }
            .flowOn(io)

    override suspend fun refreshFlights(from: String, to: String): Result<Unit> =
        withContext(io) {
            suspendRunCatching {
                // TODO: val dtos = api.searchFlights(from, to)
                val dtos = fakeFlightDtos(from, to)
                dao.upsertFlights(dtos.map { it.toEntity(from, to) })
            }
        }
}
```

### Search ViewModel split

**Search screen (`FlightViewModel`) — stays imperative; `refreshFlights` is a `Result` mutation:**

```kotlin
is FlightIntent.SearchFlights -> {
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
        setState { copy(isLoading = true, error = null) }
        refreshFlightsUseCase(from, to)
            .onSuccess { setState { copy(isLoading = false) }; setEffect(FlightEffect.NavigateToResults(from, to)) }
            .onFailure { setState { copy(isLoading = false, error = it.message) } }
    }
}
```

**Results screen (`FlightResultsViewModel`) — fully reactive; no navigation concern:**

```kotlin
override val uiState: StateFlow<FlightResultsState> =
    observeFlightsUseCase(from, to)
        .asDataResult()
        .map { result ->
            when (result) {
                DataResult.Loading    -> FlightResultsState(isLoading = true)
                is DataResult.Success -> FlightResultsState(flights = result.data.toImmutableList())
                is DataResult.Error   -> FlightResultsState(error = result.exception.message)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FlightResultsState(isLoading = true))
```

### Files to update — PR2

- [ ] `feature/flights/build.gradle.kts` — add `core:database` dependency
- [ ] `feature/stays/build.gradle.kts` — add `core:database` dependency
- [ ] `feature/flights/data/mapper/FlightMapper.kt` — add `FlightDto.toEntity()` + `FlightEntity.toDomain()`
- [ ] `feature/stays/data/mapper/StayMapper.kt` — same
- [ ] `feature/flights/domain/repository/FlightRepository.kt` — observe + refresh interface
- [ ] `feature/flights/data/repository/FlightRepositoryImpl.kt` — offline-first impl
- [ ] `feature/flights/domain/usecase/` — replace `SearchFlightsUseCase` with `ObserveFlightsUseCase` + `RefreshFlightsUseCase`
- [ ] `feature/flights/data/local/FlightLocalDataSource.kt` — replace TODO stub with real DAO reference
- [ ] `feature/flights/ui/FlightViewModel.kt` — uses `RefreshFlightsUseCase`, drops DataResult
- [ ] `feature/flights/ui/FlightResultsViewModel.kt` — **new file**, reactive stateIn pipeline
- [ ] `feature/flights/ui/FlightResultsScreen.kt` — inject `FlightResultsViewModel` instead of `FlightViewModel`
- [ ] `feature/stays/domain/repository/StayRepository.kt` — same split
- [ ] `feature/stays/data/repository/StayRepositoryImpl.kt` — same
- [ ] `feature/stays/domain/usecase/` — replace `SearchStaysUseCase`
- [ ] `feature/stays/data/local/StayLocalDataSource.kt` — replace TODO stub
- [ ] `feature/stays/ui/StayViewModel.kt` — uses `RefreshStaysUseCase`
- [ ] `feature/stays/ui/StayResultsViewModel.kt` — **new file**, reactive stateIn pipeline

### Verification — PR2

- [ ] Room in-memory test: `upsertFlights(list)` → `observeFlights().test { awaitItem() }` → assert correct items
- [ ] Offline-first test: `refreshFlights` returns `Result.failure`, pre-seed DAO → `observeFlights` still emits cache
- [ ] Navigation test: `SearchFlights` intent → assert `FlightEffect.NavigateToResults` fires exactly once per intent
- [ ] `./gradlew assembleDebug` — full build green after module dependency additions

---

## PR3 — G11 (remaining 4 features): Experiences, Bookings, Services, Home → Room

**Scope:** Complete Room integration for all remaining `*LocalDataSource.kt` stubs.
Update `TravelMonkDatabase.kt` to register all 6 entity types.

### Critical: Repository interface contracts to fix

`BookingRepository` and `ExperienceRepository` currently return `Flow<DataResult<List<T>>>` —
mixing the error-wrapping concern into the repository layer. This is wrong.
After Room, repositories return raw `Flow<List<T>>` and the ViewModel applies `asDataResult()`.

```kotlin
// Current (wrong — DataResult wrapping inside repository)
interface BookingRepository {
    fun getBookings(): Flow<DataResult<List<Booking>>>
}

// After Room (correct — repository returns raw domain type; VM wraps with asDataResult())
interface BookingRepository {
    fun observeBookings(): Flow<List<Booking>>           // Room DAO reactive read
    suspend fun refreshBookings(): Result<Unit>          // one-shot network sync
    suspend fun cancelBooking(bookingId: String): Result<Unit>  // already Result from PR1
}
```

### `core/database` — additions

- [ ] `core/database/.../entity/ExperienceEntity.kt`
- [ ] `core/database/.../entity/BookingEntity.kt`
- [ ] `core/database/.../entity/ServiceEntity.kt`
- [ ] `core/database/.../entity/HomeBannerEntity.kt`
- [ ] `core/database/.../dao/ExperienceDao.kt` — `observeByCategory(category): Flow<List<ExperienceEntity>>` + `observeById(id)` + `@Upsert`
- [ ] `core/database/.../dao/BookingDao.kt` — `observeBookings(): Flow<List<BookingEntity>>` + `@Upsert` + `cancelBooking(id)`
- [ ] `core/database/.../dao/ServiceDao.kt` — `observeServices(): Flow<List<ServiceEntity>>` + `@Upsert`
- [ ] `core/database/.../dao/HomeBannerDao.kt` — `observeBanners(): Flow<List<HomeBannerEntity>>` + `@Upsert`
- [ ] Update `TravelMonkDatabase.kt` — register all 6 entity types
- [ ] Update `DatabaseModule.kt` — provide all 6 DAOs

### Files to update — Experiences

- [ ] `feature/experiences/build.gradle.kts` — add `core:database` dependency
- [ ] `feature/experiences/domain/repository/ExperienceRepository.kt` — `observeExperiences(category): Flow<List<Experience>>` + `refreshExperiences(): Result<Unit>` + `observeExperienceById(id): Flow<Experience>`
- [ ] `feature/experiences/data/repository/ExperienceRepositoryImpl.kt` — replace `flow { emit(Loading); ... }` with DAO-backed offline-first
- [ ] `feature/experiences/data/mapper/ExperienceMapper.kt` — add `ExperienceDto.toEntity()` + `ExperienceEntity.toDomain()`
- [ ] `feature/experiences/data/local/ExperienceLocalDataSource.kt` — replace TODO stub
- [ ] `feature/experiences/domain/usecase/GetExperiencesUseCase.kt` → `ObserveExperiencesUseCase` + `RefreshExperiencesUseCase`
- [ ] `feature/experiences/domain/usecase/GetExperienceDetailsUseCase.kt` → `ObserveExperienceDetailsUseCase`
- [ ] `feature/experiences/ui/ExperienceViewModel.kt` — reactive pipeline via `asDataResult()`
- [ ] `feature/experiences/ui/ExperienceDetailsViewModel.kt` — reactive pipeline via `asDataResult()`

### Files to update — Bookings

- [ ] `feature/bookings/build.gradle.kts` — add `core:database` dependency
- [ ] `feature/bookings/domain/repository/BookingRepository.kt` — `observeBookings()` + `refreshBookings()` (cancelBooking already `Result<Unit>` from PR1)
- [ ] `feature/bookings/data/repository/BookingRepositoryImpl.kt` — DAO-backed offline-first
- [ ] `feature/bookings/data/mapper/BookingMapper.kt` — add `BookingDto.toEntity()` + `BookingEntity.toDomain()`
- [ ] `feature/bookings/data/local/BookingLocalDataSource.kt` — replace TODO stub
- [ ] `feature/bookings/domain/usecase/GetBookingsUseCase.kt` → `ObserveBookingsUseCase` (returns `Flow<List<Booking>>`)
- [ ] `feature/bookings/ui/BookingViewModel.kt` — update `flatMapLatest` pipeline to use raw `Flow<List<Booking>>.asDataResult()`

### Files to update — Services

- [ ] `feature/services/build.gradle.kts` — add `core:database` dependency
- [ ] `feature/services/domain/repository/ServiceRepository.kt` — `observeServices(): Flow<List<Service>>` + `refreshServices(): Result<Unit>`
- [ ] `feature/services/data/repository/ServiceRepositoryImpl.kt` — DAO-backed offline-first
- [ ] `feature/services/data/mapper/ServiceMapper.kt` — add `ServiceDto.toEntity()` + `ServiceEntity.toDomain()`
- [ ] `feature/services/data/local/ServiceLocalDataSource.kt` — replace TODO stub
- [ ] `feature/services/domain/usecase/` — replace with `ObserveServicesUseCase` + `RefreshServicesUseCase`
- [ ] `feature/services/ui/ServicesViewModel.kt` — reactive stateIn pipeline

### Files to update — Home

- [ ] `feature/home/build.gradle.kts` — add `core:database` dependency
- [ ] `feature/home/domain/repository/HomeRepository.kt` — `observeHomeBanners(): Flow<List<HomeBanner>>` + `refreshBanners(): Result<Unit>`
- [ ] `feature/home/data/repository/HomeRepositoryImpl.kt` — DAO-backed offline-first
- [ ] `feature/home/data/mapper/HomeMapper.kt` — add `HomeBannerDto.toEntity()` + `HomeBannerEntity.toDomain()`
- [ ] `feature/home/data/local/HomeLocalDataSource.kt` — replace TODO stub
- [ ] `feature/home/domain/usecase/` — update to observe + refresh pattern
- [ ] `feature/home/ui/HomeViewModel.kt` — reactive stateIn pipeline

### Verification — PR3

- [ ] Room in-memory tests for all 4 new DAOs: upsert → observe → assert correct items
- [ ] `BookingViewModel` reactive pipeline: pre-seed `BookingDao` → assert `BookingState(bookings = [...])` emitted
- [ ] `ExperienceViewModel` reactive pipeline: same
- [ ] `grep -rn "DataResult.Loading" feature --include="*.kt"` → only hits inside reactive ViewModel `when` branches; zero in use cases or repositories
- [ ] `./gradlew assembleDebug` — full build green

---

## Gap Coverage Summary

| Gap ID | Description | PR |
|---|---|---|
| G10 | Dead `DataResult.Loading` in all suspend mutations | PR1 |
| G11 — Flights + Stays | Search use cases → `Flow<List<T>>` + Room DAOs | PR2 |
| G11 — Experiences + Bookings + Services + Home | Remaining 4 feature Room DAOs + repository interface contracts | PR3 |
| G06 (architecture_gaps.md) | `core:database` declared but empty | PR2 + PR3 |
