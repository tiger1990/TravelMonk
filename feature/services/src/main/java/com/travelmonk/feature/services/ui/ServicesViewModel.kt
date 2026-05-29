package com.travelmonk.feature.services.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.services.domain.model.TravelService
import com.travelmonk.feature.services.domain.usecase.GetServicesUseCase
import com.travelmonk.feature.services.mvi.ServicesEffect
import com.travelmonk.feature.services.mvi.ServicesIntent
import com.travelmonk.feature.services.mvi.ServicesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import javax.inject.Inject

@HiltViewModel
class ServicesViewModel @Inject constructor(
    getServicesUseCase: GetServicesUseCase
) : BaseViewModel<ServicesState, ServicesIntent, ServicesEffect>(ServicesState()) {

    // Tracks the user's currently selected service independently of the data stream.
    // Kept as a separate MutableStateFlow so user selection is merged into the reactive
    // pipeline without triggering a re-fetch of service data.
    private val _selectedService = MutableStateFlow<TravelService?>(null)

    // Reactive pipeline using combine():
    // combine() re-emits whenever EITHER upstream updates — correct for both
    // repository data refreshes and user selections from _selectedService.
    // _selectedService has an immediate initial value (null), so combine() only
    // blocks on the first emission from getServicesUseCase().
    //
    // catch   → converts unchecked pipeline exceptions into an empty non-loading state.
    // stateIn initialValue covers the initial loading frame — do NOT add .onStart here.
    // onStart would re-fire on every upstream restart (user returns after 5 s), replacing
    // the cached success state with a loading flash before data reloads — visible flicker.
    //
    // Room migration: getServicesUseCase() will return a Room-backed Flow; combine()
    // handles it transparently, re-emitting on every DB change with the current selection.
    override val uiState: StateFlow<ServicesState> = combine(
        getServicesUseCase(),
        _selectedService
    ) { result, selectedService ->
        when (result) {
            is DataResult.Success -> ServicesState(
                services = result.data.toPersistentList(),
                selectedService = selectedService
            )
            is DataResult.Error  -> ServicesState(selectedService = selectedService)
            is DataResult.Loading -> ServicesState(isLoading = true, selectedService = selectedService)
        }
    }
    .catch { emit(ServicesState()) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ServicesState(isLoading = true)
    )

    override fun handleIntent(intent: ServicesIntent) {
        when (intent) {
            is ServicesIntent.SelectService -> viewModelScope.launch {
                // Update _selectedService first so combine() re-emits the new selection
                // into uiState. yield() suspends this coroutine once, giving the combine()
                // pipeline a chance to propagate the updated state to the UI before the
                // navigation effect is delivered — guaranteeing state is never stale
                // when the user presses Back and the screen is restored.
                _selectedService.value = intent.service
                yield()
                setEffect(ServicesEffect.NavigateToBooking(intent.service))
            }
        }
    }
}
