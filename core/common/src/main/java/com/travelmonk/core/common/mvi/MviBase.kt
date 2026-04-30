package com.travelmonk.core.common.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmonk.core.logger.TravelMonkLogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface UiState
interface UiIntent
interface UiEffect

abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect> : ViewModel() {

    init {
        TravelMonkLogger.d(
            tag = "ViewModel",
            msg = "${this::class.simpleName} created | hash=${Integer.toHexString(System.identityHashCode(this))}"
        )
    }

    private val initialState: S by lazy { createInitialState() }
    abstract fun createInitialState(): S

    private val _uiState = MutableStateFlow(initialState)
    //val uiState: StateFlow<S> = _uiState.asStateFlow()

    val uiState: StateFlow<S> by lazy {
        _uiState.onStart {
            viewModelScope.launch {
                initialDataLoad()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = initialState
        )
    }

    open suspend fun initialDataLoad() {}

    /**
     *   private val _effect = MutableSharedFlow<E>()
     *     val effect = _effect.asSharedFlow()
     */
    // Channel ensures each effect is delivered exactly once — no replay on recomposition
    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    protected val currentState: S
        get() = uiState.value

    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: I)

    protected fun setState(reduce: S.() -> S) {
        val newState = currentState.reduce()
        _uiState.value = newState
    }

    /**
     * protected suspend fun setEffect(effect: E) {
     *         _effect.emit(effect)
     *     }
     */
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
