package com.travelmonk.feature.stays.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.usecase.SearchStaysUseCase
import com.travelmonk.feature.stays.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StayViewModel @Inject constructor(
    private val searchStaysUseCase: SearchStaysUseCase
) : BaseViewModel<StaySearchState, StayIntent, StayEffect>() {
    override fun createInitialState(): StaySearchState = StaySearchState()

    override fun handleIntent(intent: StayIntent) {
        when (intent) {
            is StayIntent.ChangeStayType -> setState { copy(stayType = intent.type) }
            is StayIntent.UpdateLocation -> setState { copy(location = intent.location) }
            is StayIntent.SearchStays -> {
                viewModelScope.launch {
                    setState { copy(isLoading = true) }
                    when (val result = searchStaysUseCase(currentState.location)) {
                        is DataResult.Success -> {
                            setState { copy(isLoading = false) }
                            setEffect(StayEffect.NavigateToResults(currentState.location))
                        }
                        is DataResult.Error -> {
                            setState { copy(isLoading = false, error = result.exception.message) }
                            setEffect(StayEffect.ShowError(result.exception.message ?: "Failed to search stays"))
                        }
                        is DataResult.Loading -> Unit
                    }
                }
            }
        }
    }
}
