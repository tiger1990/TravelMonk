# Simple MVI Flow — Full Stack Reference

A complete, layer-by-layer reference for building an offline-first feature using MVI + Clean Architecture on Android.

---

## Architecture Overview

```
UI (Compose)
    │  Intent
    ▼
ViewModel (MVI Engine)
    │  UseCase calls
    ▼
Domain Layer (UseCases + Repository contract)
    │
    ▼
Data Layer (Repository impl → Room DAO ← Retrofit API)
```

**Single Source of Truth:** Room is always the source. Network only updates the cache.

---

## 1. Domain Layer

### Model

```kotlin
data class Item(
    val id: Int,
    val name: String
)
```

### Repository Contract

```kotlin
interface ItemRepository {
    fun observeItems(): Flow<List<Item>>
    suspend fun refresh()
}
```

> **Why an interface:** The domain layer must not depend on data-layer details (Room, Retrofit). The interface keeps the domain pure and lets you swap implementations or inject fakes in tests.

### Use Cases

```kotlin
class ObserveItemsUseCase @Inject constructor(
    private val repo: ItemRepository
) {
    operator fun invoke() = repo.observeItems()
}

class RefreshItemsUseCase @Inject constructor(
    private val repo: ItemRepository
) {
    suspend operator fun invoke() = repo.refresh()
}
```

> **Why separate use cases:** Each use case has a single responsibility. The ViewModel composes them rather than calling the repository directly, keeping business rules testable in isolation.

---

## 2. Data Layer

### Remote DTO

```kotlin
data class ItemDto(
    val id: Int,
    val name: String
)
```

### API

```kotlin
interface ApiService {
    @GET("items")
    suspend fun getItems(): List<ItemDto>
}
```

### Room Entity

```kotlin
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: Int,
    val name: String
)
```

### DAO

```kotlin
@Dao
interface ItemDao {

    @Query("SELECT * FROM items")
    fun observeItems(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Query("DELETE FROM items")
    suspend fun clear()
}
```

### Database

```kotlin
@Database(entities = [ItemEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
```

### Mappers

```kotlin
fun ItemDto.toEntity() = ItemEntity(id, name)
fun ItemEntity.toDomain() = Item(id, name)
```

> **Why mappers:** Each layer owns its own model. Mappers are the seam between layers — they keep DTOs, entities, and domain objects independently evolvable without leaking layer-specific concerns.

### Repository Implementation (Offline-First Core)

```kotlin
class ItemRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: ItemDao
) : ItemRepository {

    override fun observeItems(): Flow<List<Item>> {
        return dao.observeItems()
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun refresh() {
        try {
            val remote = api.getItems()
            val entities = remote.map { it.toEntity() }
            dao.clear()
            dao.insertAll(entities)
        } catch (e: Exception) {
            // Offline-first: swallow network errors silently.
            // UI continues showing cached data from Room.
        }
    }
}
```

> **Why this pattern:** `observeItems()` is a cold Room Flow — it emits every time the DB changes. `refresh()` triggers a network fetch that writes to Room, which automatically re-emits through the Flow. The UI never calls the API directly.

---

## 3. Dependency Injection (Hilt)

### Database Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDb(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_db"
        ).build()
    }

    @Provides
    fun provideDao(db: AppDatabase) = db.itemDao()
}
```

### Network Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideApi(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

### Repository Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {

    @Binds
    abstract fun bindRepo(impl: ItemRepositoryImpl): ItemRepository
}
```

> **Why `@Binds` over `@Provides`:** `@Binds` is compile-time wiring with no generated delegation code — it tells Hilt "when `ItemRepository` is requested, provide `ItemRepositoryImpl`" at zero runtime cost.

---

## 4. MVI Layer

### Intent

```kotlin
sealed class ItemIntent {
    object Load : ItemIntent()
    object Refresh : ItemIntent()
}
```

### State

```kotlin
data class ItemState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)
```

### Result (internal reducer input)

```kotlin
sealed class ItemResult {
    object Loading : ItemResult()
    data class Data(val items: List<Item>) : ItemResult()
    data class Error(val msg: String) : ItemResult()
}
```

> **Why a separate Result type:** Separates the ViewModel's internal async outcomes from the public State. The `reduce` function is a pure `(State, Result) -> State` transform — easy to unit-test without coroutines.

### Side Effect

```kotlin
sealed class ItemEffect {
    data class Toast(val msg: String) : ItemEffect()
}
```

> **Why a separate Effect channel:** Effects are one-shot events (toasts, navigation, dialogs) that must not survive recomposition. Exposing them via `SharedFlow`/`Channel` guarantees exactly-once delivery, unlike `StateFlow` which replays its last value.

---

## 5. ViewModel

```kotlin
@HiltViewModel
class ItemViewModel @Inject constructor(
    private val observeItems: ObserveItemsUseCase,
    private val refreshItems: RefreshItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ItemState())
    val state: StateFlow<ItemState> = _state

    private val _effect = MutableSharedFlow<ItemEffect>()
    val effect: SharedFlow<ItemEffect> = _effect

    init {
        observeData()
    }

    fun process(intent: ItemIntent) {
        when (intent) {
            ItemIntent.Load -> refresh()
            ItemIntent.Refresh -> refresh()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            observeItems().collect { items ->
                reduce(ItemResult.Data(items))
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            reduce(ItemResult.Loading)
            try {
                refreshItems()
            } catch (e: Exception) {
                reduce(ItemResult.Error("Sync failed"))
                _effect.emit(ItemEffect.Toast("Offline mode"))
            }
        }
    }

    private fun reduce(result: ItemResult) {
        val current = _state.value
        _state.value = when (result) {
            is ItemResult.Loading -> current.copy(isLoading = true)
            is ItemResult.Data    -> current.copy(isLoading = false, items = result.items)
            is ItemResult.Error   -> current.copy(isLoading = false, error = result.msg)
        }
    }
}
```

**Key points:**
- `observeData()` starts in `init` — Room Flow drives state automatically.
- `refresh()` triggers a network sync; errors reduce to `Error` state **and** emit a one-shot `Toast` effect.
- `reduce()` is a pure state transformer — no coroutines, no side effects.

---

## 6. UI (Compose)

```kotlin
@Composable
fun ItemScreen(vm: ItemViewModel = hiltViewModel()) {

    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // One-shot effects: collect in LaunchedEffect scoped to the composable's lifetime.
    LaunchedEffect(Unit) {
        vm.effect.collect { effect ->
            if (effect is ItemEffect.Toast) {
                Toast.makeText(context, effect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Trigger initial load once.
    LaunchedEffect(Unit) {
        vm.process(ItemIntent.Load)
    }

    when {
        state.isLoading -> CircularProgressIndicator()
        else -> {
            LazyColumn {
                items(state.items.size) { index ->
                    Text(state.items[index].name)
                }
            }
        }
    }
}
```

> **Why `collectAsStateWithLifecycle()`:** Stops Flow collection when the lifecycle drops below `STARTED` (app backgrounded), preventing unnecessary UI updates. Prefer this over `collectAsState()` in all Android Compose screens.

---

## 7. What Makes This Senior-Level

| Principle | How it's applied |
|---|---|
| **Offline-first** | UI always reads from Room; network only updates cache |
| **Single Source of Truth** | Room is the only source — no direct API → UI path |
| **Resilient** | Network failures are swallowed; UI degrades gracefully on cached data |
| **Pure reducer** | `reduce()` is a side-effect-free `(State, Result) -> State` function |
| **Exactly-once effects** | `SharedFlow` for toasts/navigation — no replayed events on recomposition |
| **Lifecycle-aware** | `collectAsStateWithLifecycle()` stops work when the app is backgrounded |
| **Testable seams** | Repository interface, use cases, and pure reducer are all independently testable |

---

## 8. Next Steps (Advanced Topics)

| Topic | Why |
|---|---|
| **Paging 3 + RemoteMediator** | True offline pagination — Room as paging source, network as mediator |
| **Sync strategies** | Stale-while-revalidate, TTL-based invalidation |
| **Multi-module architecture** | Feature isolation, build-time decoupling |
| **Compose recomposition tuning** | `@Stable`, `@Immutable`, `derivedStateOf` traps |
| **Full test suite** | Turbine (Flow testing) + Fake repository + ViewModel unit tests |
