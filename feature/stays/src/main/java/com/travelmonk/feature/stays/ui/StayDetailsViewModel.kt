package com.travelmonk.feature.stays.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.repository.StayRepository
import com.travelmonk.feature.stays.mvi.StayDetailsEffect
import com.travelmonk.feature.stays.mvi.StayDetailsIntent
import com.travelmonk.feature.stays.mvi.StayDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StayDetailsViewModel @Inject constructor(
    private val repository: StayRepository
) : BaseViewModel<StayDetailsState, StayDetailsIntent, StayDetailsEffect>() {

    override fun createInitialState(): StayDetailsState = StayDetailsState()

    override fun handleIntent(intent: StayDetailsIntent) {
        when (intent) {
            is StayDetailsIntent.LoadDetails -> loadStayDetails(intent.stayId)
            is StayDetailsIntent.BookNow -> {
                uiState.value.stay?.let {
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

    private fun loadStayDetails(stayId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = repository.getStayById(stayId)) {
                is DataResult.Success -> {
                    setState { copy(stay = result.data, isLoading = false) }
                }
                is DataResult.Error -> {
                    setState { 
                        copy(
                            isLoading = false, 
                            error = result.exception.message ?: "Failed to load stay details" 
                        ) 
                    }
                    setEffect(StayDetailsEffect.ShowError(result.exception.message ?: "An error occurred"))
                }
                is DataResult.Loading -> {
                    setState { copy(isLoading = true) }
                }
            }
        }
    }
}
