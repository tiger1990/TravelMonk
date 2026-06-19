package com.travelmonk.feature.experiences.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.domain.usecase.GetExperienceDetailsUseCase
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsEffect
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsIntent
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsState
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

@HiltViewModel(assistedFactory = ExperienceDetailsViewModel.Factory::class)
class ExperienceDetailsViewModel @AssistedInject constructor(
    @Assisted private val experienceId: String,
    private val getExperienceDetailsUseCase: GetExperienceDetailsUseCase
) : BaseViewModel<ExperienceDetailsState, ExperienceDetailsIntent, ExperienceDetailsEffect>(ExperienceDetailsState()) {

    @AssistedFactory
    interface Factory {
        fun create(experienceId: String): ExperienceDetailsViewModel
    }

    // replay = 1 so a late subscriber receives the id seeded in init{}: the screen subscribes via
    // collectAsStateWithLifecycle AFTER construction, and WhileSubscribed re-subscribes after a
    // backgrounding. With replay = 0 the init{} emit fired before any collector and was dropped,
    // leaving the screen stuck on the initial Loading frame.
    // G3: SharedFlow (unlike StateFlow) still re-delivers the SAME id on retry-after-error —
    // there is no distinctUntilChanged to swallow an unchanged value.
    private val _experienceIdSignal = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        // experienceId now arrives via @AssistedInject from the nav key. Seed the load once here,
        // so rotation no longer re-fetches (the old LaunchedEffect-in-screen re-fired per config change).
        _experienceIdSignal.tryEmit(experienceId)
    }

    override val uiState: StateFlow<ExperienceDetailsState> = _experienceIdSignal
        .flatMapLatest { id ->
            getExperienceDetailsUseCase(id)
                .onEach { result ->
                    if (result is DataResult.Error) {
                        setEffect(ExperienceDetailsEffect.ShowError(result.exception.message ?: "Failed to load details"))
                    }
                }
                .map { result ->
                    when (result) {
                        is DataResult.Loading  -> ExperienceDetailsState(isLoading = true)
                        is DataResult.Success  -> ExperienceDetailsState(experience = result.data, isLoading = false)
                        is DataResult.Error    -> ExperienceDetailsState(isLoading = false, error = result.exception.message)
                    }
                }
        }
        // No .onStart needed: stateIn's initialValue covers the initial loading frame, and replay = 1
        // means a re-subscribe (after WhileSubscribed's 5 s timeout) replays the last id, so
        // flatMapLatest re-runs the load instead of leaving the UI stuck on loading.
        .catch { t -> emit(ExperienceDetailsState(error = t.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExperienceDetailsState(isLoading = true)
        )

    override fun handleIntent(intent: ExperienceDetailsIntent) {
        when (intent) {
            // Retired — initial load now seeded in init{} from the assisted experienceId:
            // is ExperienceDetailsIntent.LoadDetails -> _experienceIdSignal.tryEmit(intent.id)
            // Retry re-emits the stored id (preserves the G3 re-emit-same-id behavior).
            is ExperienceDetailsIntent.Retry -> _experienceIdSignal.tryEmit(experienceId)
            is ExperienceDetailsIntent.BookNow -> {
                currentState.experience?.let {
                    viewModelScope.launch {
                        setEffect(ExperienceDetailsEffect.NavigateToBooking(it))
                    }
                }
            }
        }
    }
}
