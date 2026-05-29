package com.travelmonk.core.common.mvi

import androidx.lifecycle.ViewModel
import com.travelmonk.core.logger.TravelMonkLogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

interface UiState
interface UiIntent
interface UiEffect

abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)

    init {
        TravelMonkLogger.d(
            tag = "ViewModel",
            msg = "${this::class.simpleName} created | hash=${Integer.toHexString(System.identityHashCode(this))}"
        )
    }

    // Default: imperative state via setState(). Subclasses whose repository returns a
    // reactive Flow<T> should override this with a stateIn() pipeline instead:
    //
    //   override val uiState: StateFlow<S> = useCase()
    //       .map { result -> /* map DataResult → State */ }
    //       .onStart { emit(State(isLoading = true)) }
    //       .catch  { emit(State(error = it.message)) }
    //       .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State(isLoading = true))
    //
    // WhileSubscribed(5_000) survives config changes (rotation < 5s) without re-triggering the
    // load. onStart is safe here because it emits a state VALUE, not a side-effecting coroutine.
    // NOTE: when overriding, setState() / currentState target _uiState and are no longer the
    // source of truth. Use ViewModel-local MutableStateFlow for any supplementary state.
    open val uiState: StateFlow<S> = _uiState.asStateFlow()

    // Channel ensures each effect is delivered exactly once — no replay on recomposition
    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Delegates to the open `uiState` rather than `_uiState` directly.
    // Imperative subclasses (setState-based): uiState == _uiState.asStateFlow(), so value is identical.
    // Reactive subclasses (stateIn override): uiState.value returns the correct live state from the
    // stateIn pipeline, not the frozen initialState stored in _uiState which setState never updates.
    protected val currentState: S
        get() = uiState.value

    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: I)

    protected fun setState(reduce: S.() -> S) {
        _uiState.update { it.reduce() }
    }

    protected suspend fun setEffect(effect: E) {
        _effect.send(effect)
    }

    override fun onCleared() {
        TravelMonkLogger.d(
            tag = "ViewModel",
            msg = "${this::class.simpleName} cleared | hash=${Integer.toHexString(System.identityHashCode(this))}"
        )
        super.onCleared()
        _effect.close()
    }
}