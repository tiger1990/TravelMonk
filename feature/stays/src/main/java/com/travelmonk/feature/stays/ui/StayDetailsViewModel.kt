package com.travelmonk.feature.stays.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.usecase.GetStayDetailsUseCase
import com.travelmonk.feature.stays.mvi.StayDetailsEffect
import com.travelmonk.feature.stays.mvi.StayDetailsIntent
import com.travelmonk.feature.stays.mvi.StayDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StayDetailsViewModel @Inject constructor(
    private val getStayDetailsUseCase: GetStayDetailsUseCase
) : BaseViewModel<StayDetailsState, StayDetailsIntent, StayDetailsEffect>(StayDetailsState()) {

    override fun handleIntent(intent: StayDetailsIntent) {
        when (intent) {
            is StayDetailsIntent.LoadDetails -> loadStayDetails(intent.stayId)
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

    private fun loadStayDetails(stayId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            when (val result = getStayDetailsUseCase(stayId)) {
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
