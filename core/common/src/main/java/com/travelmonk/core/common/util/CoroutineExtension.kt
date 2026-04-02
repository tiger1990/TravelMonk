package com.travelmonk.core.common.util

/**
 * How To Utilize flow for api-call pattern
 *
// Approach1 (clean + scalable + modern Android)
 *
 * sealed class UiState<out T> {
 *     object Loading : UiState<Nothing>()
 *     data class Success<T>(val data: T) : UiState<T>()
 *     data class Error(val message: String) : UiState<Nothing>()
 * }
 *
 * class UserViewModel(
 *     private val repository: UserRepository
 * ) : ViewModel() {
 *
 *     private val _uiState = MutableStateFlow<UiState<User>>(UiState.Loading)
 *     val uiState: StateFlow<UiState<User>> = _uiState
 *
 *     fun loadUser() {
 *         _uiState.value = UiState.Loading
 *
 *         viewModelScope.networkLaunch(
 *             onError = { throwable ->
 *                 _uiState.value = UiState.Error(
 *                     throwable.message ?: "Something went wrong"
 *                 )
 *             },
 *             networkBlock = {
 *                 repository.getUserProfile()
 *             },
 *             processingBlock = { user ->
 *                 val processedUser = processUser(user)
 *                _uiState.value = UiState.Success(processedUser)
 *             }
 *         )
 *     }
 *
 *     private fun processUser(user: User): User {
 *         // Example transformation
 *         return user.copy(name = user.name.trim())
 *     }
 * }
 *
 * class UserRepository(
 *     private val api: ApiService
 * ) {
 *     suspend fun getUserProfile(): User {
 *         return api.getUserProfile()
 *     }
 * }
 * fun CoroutineScope.safeLaunch(
 *     context: CoroutineContext = EmptyCoroutineContext,
 *     onError: (Throwable) -> Unit = {},
 *     block: suspend CoroutineScope.() -> Unit
 * ): Job {
 *     val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
 *         onError(throwable)
 *     }
 *
 *     return launch(context + exceptionHandler) {
 *         try {
 *             block()
 *         } catch (e: CancellationException) {
 *             // Always rethrow cancellation exceptions
 *             throw e
 *         } catch (t: Throwable) {
 *             onError(t)
 *         }
 *     }
 * }
 *
 * fun <T> CoroutineScope.networkLaunch(
 *     context: CoroutineContext = Dispatchers.IO,
 *     onError: (Throwable) -> Unit,
 *     networkBlock: suspend () -> T,
 *     processingBlock: suspend (T) -> Unit
 * ): Job {
 *     return safeLaunch(context, onError) {
 *         // Step 1: Execute network call (IO)
 *         val result = networkBlock()
 *
 *         // Step 2: Ensure coroutine is still active
 *         ensureActive()
 *
 *         val processed = withContext(Dispatchers.Default) {
 *             result // default: no-op (can transform here if needed)
 *         }
 *
 *         // Step 3: Always switch to Main for final consumption
 *         withContext(Dispatchers.Main) {
 *             processingBlock(processed)
 *         }
 *     }
 * }

// Suggested: Approach2 (clean + scalable + modern Android)

 *class UserViewModel(
 *     private val repository: UserRepository
 * ) : ViewModel() {
 *
 *     private val _uiState = MutableStateFlow<UiState<User>>(UiState.Loading)
 *     val uiState: StateFlow<UiState<User>> = _uiState
 *
 *     fun loadUser() {
 *         networkFlow(
 *             networkCall = { repository.getUserProfile() },
 *             mapper = { user -> processUser(user) }
 *         )
 *             .onEach { state ->
 *                 _uiState.value = state
 *             }
 *             .launchIn(viewModelScope)
 *     }
 *
 *     private fun processUser(user: User): User {
 *         return user.copy(name = user.name.trim())
 *     }
 * }
 *
 * class UserRepository(
 *     private val api: ApiService
 * ) {
 *     suspend fun getUserProfile(): User {
 *         return api.getUserProfile()
 *     }
 * }
 * sealed class UiState<out T> {
 *     object Loading : UiState<Nothing>()
 *     data class Success<T>(val data: T) : UiState<T>()
 *     data class Error(val message: String) : UiState<Nothing>()
 * }
 *
 * fun <T, R> networkFlow(
 *     networkCall: suspend () -> T,
 *     mapper: (T) -> R
 * ): Flow<UiState<R>> = flow {
 *     emit(UiState.Loading)
 *
 *     val result = withContext(Dispatchers.IO) {
 *         networkCall()
 *     }
 *
 *     val mapped = withContext(Dispatchers.Default) {
 *         mapper(result)
 *     }
 *
 *     emit(UiState.Success(mapped))
 * }.catch { throwable ->
 *     emit(UiState.Error(throwable.message ?: "Something went wrong"))
 * }
 *
 */
