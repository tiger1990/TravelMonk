package com.travelmonk.feature.experiences.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.domain.usecase.GetExperienceDetailsUseCase
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsEffect
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsIntent
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperienceDetailsViewModel @Inject constructor(
    private val getExperienceDetailsUseCase: GetExperienceDetailsUseCase
) : BaseViewModel<ExperienceDetailsState, ExperienceDetailsIntent, ExperienceDetailsEffect>() {

    override fun createInitialState(): ExperienceDetailsState = ExperienceDetailsState()

    override fun handleIntent(intent: ExperienceDetailsIntent) {
        when (intent) {
            is ExperienceDetailsIntent.LoadDetails -> {
                viewModelScope.launch {
                    setState { copy(isLoading = true) }
                    when (val result = getExperienceDetailsUseCase(intent.id)) {
                        is DataResult.Success -> setState { copy(experience = result.data, isLoading = false) }
                        is DataResult.Error -> {
                            setState { copy(isLoading = false, error = result.exception.message) }
                            setEffect(ExperienceDetailsEffect.ShowError(result.exception.message ?: "Failed to load details"))
                        }
                        is DataResult.Loading -> Unit
                    }
                }
            }
            is ExperienceDetailsIntent.BookNow -> {
                viewModelScope.launch {
                    currentState.experience?.let {
                        setEffect(ExperienceDetailsEffect.NavigateToBooking(it))
                    }
                }
            }
        }
    }
}
