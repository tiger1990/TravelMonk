package com.travelmonk.feature.stays.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.usecase.SearchStaysUseCase
import com.travelmonk.feature.stays.mvi.StayResultsEffect
import com.travelmonk.feature.stays.mvi.StayResultsIntent
import com.travelmonk.feature.stays.mvi.StayResultsState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = StayResultsViewModel.Factory::class)
class StayResultsViewModel @AssistedInject constructor(
    @Assisted private val location: String,
    private val searchStaysUseCase: SearchStaysUseCase
) : BaseViewModel<StayResultsState, StayResultsIntent, StayResultsEffect>(
    // Seed location so the top bar shows it from frame one (was previously set by the LoadStays intent).
    StayResultsState(location = location)
) {

    @AssistedFactory
    interface Factory {
        fun create(location: String): StayResultsViewModel
    }

    init {
        // location now arrives via @AssistedInject from the nav key. Load once here,
        // so rotation no longer re-fetches (the old LaunchedEffect-in-screen re-fired per config change).
        loadStays()
    }

    private fun loadStays() {
        viewModelScope.launch {
            // Clear any prior error so a Retry doesn't leave a stale error banner on success.
            setState { copy(isLoading = true, error = null) }
            when (val result = searchStaysUseCase(location)) {
                is DataResult.Success -> {
                    setState { copy(isLoading = false, stays = result.data.toPersistentList()) }
                }
                is DataResult.Error -> {
                    setState { copy(isLoading = false, error = result.exception.message) }
                    setEffect(StayResultsEffect.ShowError(result.exception.message ?: "Failed to load stays"))
                }
                // suspend use case — Loading is not a terminal value; isLoading was set before this call.
                is DataResult.Loading -> Unit
            }
        }
    }

    override fun handleIntent(intent: StayResultsIntent) {
        when (intent) {
            // Retired — initial load now seeded in init{} from the assisted location:
            // is StayResultsIntent.LoadStays -> { ... searchStaysUseCase(intent.location) ... }
            // Retry reloads the stored location.
            is StayResultsIntent.Retry -> loadStays()
            is StayResultsIntent.ToggleFavorite -> {
                // TODO: Implement favorite logic
            }
            is StayResultsIntent.SelectStay -> {
                viewModelScope.launch {
                    setEffect(StayResultsEffect.NavigateToDetail(intent.stay.id))
                }
            }
        }
    }
}
