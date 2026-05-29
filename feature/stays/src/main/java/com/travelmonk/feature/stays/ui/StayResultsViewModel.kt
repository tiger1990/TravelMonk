package com.travelmonk.feature.stays.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.usecase.SearchStaysUseCase
import com.travelmonk.feature.stays.mvi.StayResultsEffect
import com.travelmonk.feature.stays.mvi.StayResultsIntent
import com.travelmonk.feature.stays.mvi.StayResultsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StayResultsViewModel @Inject constructor(
    private val searchStaysUseCase: SearchStaysUseCase
) : BaseViewModel<StayResultsState, StayResultsIntent, StayResultsEffect>(StayResultsState()) {

    override fun handleIntent(intent: StayResultsIntent) {
        when (intent) {
            is StayResultsIntent.LoadStays -> {
                viewModelScope.launch {
                    setState { copy(isLoading = true, location = intent.location) }
                    when (val result = searchStaysUseCase(intent.location)) {
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
