package com.travelmonk.feature.experiences.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory as DomainCategory
import com.travelmonk.feature.experiences.domain.repository.ExperienceRepository
import com.travelmonk.feature.experiences.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperienceViewModel @Inject constructor(
    private val experienceRepository: ExperienceRepository
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
            try {
                val domainCategory = when(category) {
                    ExperienceCategory.PACKAGES -> DomainCategory.PACKAGES
                    ExperienceCategory.GUIDES -> DomainCategory.GUIDES
                    ExperienceCategory.YOGA -> DomainCategory.YOGA
                }
                val domainItems = experienceRepository.getExperiences(domainCategory)
                val uiItems = domainItems.map { 
                    ExperienceItem(it.id, it.title, it.description, it.price, it.rating, it.imageUrl)
                }
                setState { copy(items = uiItems) }
            } catch (e: Exception) {
                // Handle error
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }
}
