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
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperienceViewModel @Inject constructor(
    private val getExperiencesUseCase: GetExperiencesUseCase,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<ExperienceState, ExperienceIntent, ExperienceEffect>(
    // Restore selected category from SavedStateHandle on process-death recreation so
    // the correct tab is active immediately, before the reactive pipeline emits.
    ExperienceState(selectedCategory = initialCategory(savedStateHandle))
) {

    // Source-of-truth for category selection. Initialised from SavedStateHandle so that
    // after process death the correct category's data is loaded automatically.
    // Each emission causes flatMapLatest to cancel the in-flight stream and restart.
    private val _selectedCategory = MutableStateFlow(initialCategory(savedStateHandle))

    // Reactive pipeline using flatMapLatest():
    // Every _selectedCategory emission cancels the previous in-flight use-case stream and
    // starts fresh — stale results from a previous category never reach the UI.
    //
    // GetExperiencesUseCase returns Flow<DataResult<T>> that emits Loading then the result.
    // The flow{} wrapper is gone — Loading is now a proper first emission, not a terminal
    // value. DataResult.Loading is handled symmetrically with Success and Error.
    //
    // onEach fires the ShowError effect before map produces the state. The Channel is
    // BUFFERED so setEffect() does not block; state and effect are both delivered to the UI.
    //
    // Room migration: repository replaces flow{emit(Loading); emit(Success(fake))} with
    //   dao.getExperiences(category).map { DataResult.Success(it.map { e -> e.toDomain() }) }
    // Zero ViewModel changes needed at that point.
    override val uiState: StateFlow<ExperienceState> = _selectedCategory
        .flatMapLatest { category ->
            getExperiencesUseCase(category)
                .onEach { result ->
                    if (result is DataResult.Error) {
                        // Channel delivers this effect exactly once — no replay on recomposition.
                        setEffect(ExperienceEffect.ShowError(
                            result.exception.message ?: "Failed to load experiences"
                        ))
                    }
                }
                .map { result ->
                    when (result) {
                        is DataResult.Loading -> ExperienceState(isLoading = true, selectedCategory = category)
                        is DataResult.Success -> ExperienceState(
                            items = result.data.toPersistentList(),
                            selectedCategory = category
                        )
                        is DataResult.Error -> ExperienceState(
                            error = result.exception.message,
                            selectedCategory = category
                        )
                    }
                }
        }
        // Catches exceptions thrown by the use case flow itself (unexpected runtime throws).
        .catch { t ->
            emit(ExperienceState(
                error = t.message,
                selectedCategory = _selectedCategory.value
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExperienceState(
                isLoading = true,
                selectedCategory = _selectedCategory.value
            )
        )

    override fun handleIntent(intent: ExperienceIntent) {
        when (intent) {
            is ExperienceIntent.SelectCategory -> {
                // Persist the selection so process-death restoration loads the right category.
                savedStateHandle[KEY_SELECTED_CATEGORY] = intent.category.name
                // Updating the source flow triggers flatMapLatest to cancel the current
                // stream and start a new one for the newly selected category.
                _selectedCategory.value = intent.category
            }
            is ExperienceIntent.SelectExperience -> viewModelScope.launch {
                setEffect(ExperienceEffect.NavigateToDetail(intent.experienceId))
            }
            is ExperienceIntent.BookItem -> viewModelScope.launch {
                setEffect(ExperienceEffect.NavigateToBooking(intent.item))
            }
        }
    }

    companion object {
        private const val KEY_SELECTED_CATEGORY = "selected_category"

        // Extracted here so the same SavedStateHandle lookup is reused in both
        // the super() call and the _selectedCategory initialiser without duplication.
        fun initialCategory(savedStateHandle: SavedStateHandle): ExperienceCategory =
            savedStateHandle.get<String>(KEY_SELECTED_CATEGORY)
                ?.let { name -> ExperienceCategory.entries.firstOrNull { it.name == name } }
                ?: ExperienceCategory.PACKAGES
    }
}
