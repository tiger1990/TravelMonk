package com.travelmonk.feature.stays.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.usecase.GetStayDetailsUseCase
import com.travelmonk.feature.stays.mvi.StayDetailsEffect
import com.travelmonk.feature.stays.mvi.StayDetailsIntent
import com.travelmonk.feature.stays.mvi.StayDetailsState
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
import javax.inject.Inject

@HiltViewModel
class StayDetailsViewModel @Inject constructor(
    private val getStayDetailsUseCase: GetStayDetailsUseCase
) : BaseViewModel<StayDetailsState, StayDetailsIntent, StayDetailsEffect>(StayDetailsState()) {

    // G3: MutableSharedFlow re-emits the same stayId on retry-after-error — MutableStateFlow would
    // silently drop it via distinctUntilChanged when the value hasn't changed.
    private val _stayIdSignal = MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

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
        // NOTE: No .onStart here. stateIn's initialValue covers the initial loading frame.
        // onStart re-fires on every upstream restart (app returns after 5 s): because
        // _stayIdSignal is a MutableSharedFlow with no replay, flatMapLatest receives
        // no new ID after restart, leaving the UI stuck on loading forever.
        .catch { t -> emit(StayDetailsState(error = t.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StayDetailsState(isLoading = true)
        )

    override fun handleIntent(intent: StayDetailsIntent) {
        when (intent) {
            is StayDetailsIntent.LoadDetails -> _stayIdSignal.tryEmit(intent.stayId)
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
