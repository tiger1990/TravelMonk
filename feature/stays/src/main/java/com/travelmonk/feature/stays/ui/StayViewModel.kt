package com.travelmonk.feature.stays.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
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
                    try {
                        val results = searchStaysUseCase(currentState.location)
                        setEffect(StayEffect.NavigateToResults(currentState.location))
                    } catch (e: Exception) {
                        // Handle error
                    } finally {
                        setState { copy(isLoading = false) }
                    }
                }
            }
        }
    }
}
