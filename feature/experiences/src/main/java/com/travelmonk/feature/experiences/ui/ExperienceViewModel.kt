package com.travelmonk.feature.experiences.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import com.travelmonk.feature.experiences.domain.usecase.GetExperiencesUseCase
import com.travelmonk.feature.experiences.mvi.ExperienceEffect
import com.travelmonk.feature.experiences.mvi.ExperienceIntent
import com.travelmonk.feature.experiences.mvi.ExperienceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperienceViewModel @Inject constructor(
    private val getExperiencesUseCase: GetExperiencesUseCase
) : BaseViewModel<ExperienceState, ExperienceIntent, ExperienceEffect>() {
    override fun createInitialState(): ExperienceState = ExperienceState()

    init {
        loadItems(ExperienceCategory.PACKAGES)
    }

    override fun handleIntent(intent: ExperienceIntent) {
        when (intent) {
            is ExperienceIntent.SelectCategory -> {
                setState { copy(selectedCategory = intent.category) }
                loadItems(intent.category)
            }
            is ExperienceIntent.BookItem -> {
                viewModelScope.launch {
                    setEffect(ExperienceEffect.NavigateToBooking(intent.item))
                }
            }
        }
    }

    private fun loadItems(category: ExperienceCategory) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (val result = getExperiencesUseCase(category)) {
                is DataResult.Success -> setState { copy(items = result.data, isLoading = false) }
                is DataResult.Error -> setState { copy(isLoading = false) }
                is DataResult.Loading -> Unit
            }
        }
    }
}
