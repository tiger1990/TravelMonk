package com.travelmonk.feature.experiences.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.domain.usecase.GetExperienceDetailsUseCase
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsEffect
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsIntent
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsState
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
class ExperienceDetailsViewModel @Inject constructor(
    private val getExperienceDetailsUseCase: GetExperienceDetailsUseCase
) : BaseViewModel<ExperienceDetailsState, ExperienceDetailsIntent, ExperienceDetailsEffect>(ExperienceDetailsState()) {

    // G3: MutableSharedFlow re-emits the same id on retry-after-error — MutableStateFlow would
    // silently drop it via distinctUntilChanged when the value hasn't changed.
    private val _experienceIdSignal = MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

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
        // NOTE: No .onStart here. stateIn's initialValue covers the initial loading frame.
        // onStart re-fires on every upstream restart (app returns after 5 s): because
        // _experienceIdSignal is a MutableSharedFlow with no replay, flatMapLatest receives
        // no new ID after restart, leaving the UI stuck on loading forever.
        .catch { t -> emit(ExperienceDetailsState(error = t.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExperienceDetailsState(isLoading = true)
        )

    override fun handleIntent(intent: ExperienceDetailsIntent) {
        when (intent) {
            is ExperienceDetailsIntent.LoadDetails -> _experienceIdSignal.tryEmit(intent.id)
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
