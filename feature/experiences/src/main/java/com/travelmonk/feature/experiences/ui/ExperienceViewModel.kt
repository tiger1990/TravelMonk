package com.travelmonk.feature.experiences.ui

import androidx.lifecycle.SavedStateHandle
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
    private val getExperiencesUseCase: GetExperiencesUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<ExperienceState, ExperienceIntent, ExperienceEffect>(
    ExperienceState(
        selectedCategory = savedStateHandle.get<String>(KEY_SELECTED_CATEGORY)
            ?.let { name -> ExperienceCategory.entries.firstOrNull { it.name == name } }
            ?: ExperienceCategory.PACKAGES
    )
) {

    override suspend fun initialDataLoad() {
        loadItems(currentState.selectedCategory)
    }

    override fun handleIntent(intent: ExperienceIntent) {
        when (intent) {
            is ExperienceIntent.SelectCategory -> {
                setState { copy(selectedCategory = intent.category) }
                savedStateHandle[KEY_SELECTED_CATEGORY] = intent.category.name
                loadItems(intent.category)
            }
            is ExperienceIntent.SelectExperience -> {
                viewModelScope.launch {
                    setEffect(ExperienceEffect.NavigateToDetail(intent.experienceId))
                }
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
                is DataResult.Error -> {
                    setState { copy(isLoading = false, error = result.exception.message) }
                    setEffect(ExperienceEffect.ShowError(result.exception.message ?: "Failed to load experiences"))
                }
                is DataResult.Loading -> Unit
            }
        }
    }

    companion object {
        private const val KEY_SELECTED_CATEGORY = "selected_category"
    }
}
