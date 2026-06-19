package com.travelmonk.feature.stays.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.usecase.GetStayDetailsUseCase
import com.travelmonk.feature.stays.mvi.StayDetailsEffect
import com.travelmonk.feature.stays.mvi.StayDetailsIntent
import com.travelmonk.feature.stays.mvi.StayDetailsState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = StayDetailsViewModel.Factory::class)
class StayDetailsViewModel @AssistedInject constructor(
    @Assisted private val stayId: String,
    private val getStayDetailsUseCase: GetStayDetailsUseCase
) : BaseViewModel<StayDetailsState, StayDetailsIntent, StayDetailsEffect>(StayDetailsState()) {

    @AssistedFactory
    interface Factory {
        fun create(stayId: String): StayDetailsViewModel
    }

    // replay = 1 so a late subscriber receives the id seeded in init{}: the screen subscribes via
    // collectAsStateWithLifecycle AFTER construction, and WhileSubscribed re-subscribes after a
    // backgrounding. With replay = 0 the init{} emit fired before any collector and was dropped,
    // leaving the screen stuck on the initial Loading frame.
    // G3: SharedFlow (unlike StateFlow) still re-delivers the SAME stayId on retry-after-error —
    // there is no distinctUntilChanged to swallow an unchanged value.
    private val _stayIdSignal = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        // stayId now arrives via @AssistedInject from the nav key. Seed the load once here,
        // so rotation no longer re-fetches (the old LaunchedEffect-in-screen re-fired per config change).
        _stayIdSignal.tryEmit(stayId)
    }

    override val uiState: StateFlow<StayDetailsState> = _stayIdSignal
        .flatMapLatest { stayId ->
            getStayDetailsUseCase(stayId)
                .onEach { result ->
                    if (result is DataResult.Error) {
                        setEffect(StayDetailsEffect.ShowError(result.exception.message ?: "An error occurred"))
                    }
                }
                .map { result ->
                    when (result) {
                        is DataResult.Loading  -> StayDetailsState(isLoading = true)
                        is DataResult.Success  -> StayDetailsState(stay = result.data, isLoading = false)
                        is DataResult.Error    -> StayDetailsState(isLoading = false, error = result.exception.message)
                    }
                }
        }
        // No .onStart needed: stateIn's initialValue covers the initial loading frame, and replay = 1
        // means a re-subscribe (after WhileSubscribed's 5 s timeout) replays the last id, so
        // flatMapLatest re-runs the load instead of leaving the UI stuck on loading.
        .catch { t -> emit(StayDetailsState(error = t.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StayDetailsState(isLoading = true)
        )

    override fun handleIntent(intent: StayDetailsIntent) {
        when (intent) {
            // Retired — initial load now seeded in init{} from the assisted stayId:
            // is StayDetailsIntent.LoadDetails -> _stayIdSignal.tryEmit(intent.stayId)
            // Retry re-emits the stored id (preserves the G3 re-emit-same-id behavior).
            is StayDetailsIntent.Retry -> _stayIdSignal.tryEmit(stayId)
            is StayDetailsIntent.BookNow -> {
                currentState.stay?.let {
                    viewModelScope.launch {
                        setEffect(StayDetailsEffect.NavigateToBooking(it))
                    }
                }
            }
            is StayDetailsIntent.ToggleFavorite -> {
                // Handle favorite toggle logic
            }
        }
    }
}
