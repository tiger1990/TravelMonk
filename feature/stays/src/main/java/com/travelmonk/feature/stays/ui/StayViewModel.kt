package com.travelmonk.feature.stays.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.usecase.SearchStaysUseCase
import com.travelmonk.feature.stays.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StayViewModel @Inject constructor(
    private val searchStaysUseCase: SearchStaysUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<StaySearchState, StayIntent, StayEffect>(
    StaySearchState(
        location = savedStateHandle[KEY_LOCATION] ?: "Paris, France",
        stayType = savedStateHandle.get<String>(KEY_STAY_TYPE)
            ?.let { name -> StayType.entries.firstOrNull { it.name == name } }
            ?: StayType.HOTEL
    )
) {

    // Each emission carries the search location; flatMapLatest auto-cancels any
    // in-flight searchStaysUseCase call when SearchStays fires again — no manual Job needed.
    // G7: DROP_OLDEST ensures the latest search always wins when emissions arrive faster than the consumer
    private val _searchTrigger = MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        viewModelScope.launch {
            _searchTrigger
                .flatMapLatest { location ->
                    flow {
                        emit(DataResult.Loading)
                        emit(searchStaysUseCase(location))
                    }
                }
                .collect { result ->
                    when (result) {
                        is DataResult.Loading -> setState { copy(isLoading = true, error = null) }
                        is DataResult.Success -> {
                            setState { copy(isLoading = false) }
                            setEffect(StayEffect.NavigateToResults(currentState.location))
                        }
                        is DataResult.Error -> {
                            setState { copy(isLoading = false, error = result.exception.message) }
                            setEffect(StayEffect.ShowError(result.exception.message ?: "Failed to search stays"))
                        }
                    }
                }
        }
    }

    override fun handleIntent(intent: StayIntent) {
        when (intent) {
            is StayIntent.ChangeStayType -> {
                setState { copy(stayType = intent.type) }
                savedStateHandle[KEY_STAY_TYPE] = intent.type.name
            }
            is StayIntent.UpdateLocation -> {
                setState { copy(location = intent.location) }
                savedStateHandle[KEY_LOCATION] = intent.location
            }
            is StayIntent.SearchStays -> _searchTrigger.tryEmit(currentState.location)
        }
    }

    companion object {
        private const val KEY_LOCATION  = "location"
        private const val KEY_STAY_TYPE = "stay_type"
    }
}
