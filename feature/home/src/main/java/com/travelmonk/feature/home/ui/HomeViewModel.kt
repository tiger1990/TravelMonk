package com.travelmonk.feature.home.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.home.domain.usecase.GetHomeBannersUseCase
import com.travelmonk.feature.home.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getHomeBannersUseCase: GetHomeBannersUseCase
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>(
    // Categories are static UI config — provided upfront so the category row renders
    // immediately without waiting for the banner stream to emit.
    HomeState(categories = staticCategories, isLoading = true)
) {

    // Reactive pipeline: getHomeBannersUseCase() returns a cold Flow<DataResult<T>>.
    //
    // map    → translates each DataResult into a complete HomeState snapshot.
    // catch  → converts unchecked exceptions to Error state instead of crashing.
    // stateIn(WhileSubscribed(5_000)) → keeps the upstream alive through config changes
    //          (rotation completes well within 5 s). After 5 s of no subscribers the
    //          upstream stops; on return the cached value is served immediately and
    //          the upstream restarts without emitting a Loading flash.
    //
    // NOTE: Do NOT add .onStart { emit(loading) } here. stateIn's initialValue already
    // handles the empty-frame case on first load. onStart would re-fire on every upstream
    // restart (i.e. every time the user returns after 5 s), replacing the cached success
    // state with a loading flash before data reloads — visible flicker.
    //
    // Room migration: when Room is added, getHomeBannersUseCase() returns a Room-backed
    // Flow that re-emits on DB writes. Zero ViewModel changes needed at that point.
    override val uiState: StateFlow<HomeState> = getHomeBannersUseCase()
        .map { result ->
            when (result) {
                is DataResult.Success -> HomeState(
                    categories = staticCategories,
                    banners = result.data.toPersistentList()
                )
                is DataResult.Error -> HomeState(
                    categories = staticCategories,
                    error = result.exception.message
                )
                is DataResult.Loading -> HomeState(
                    categories = staticCategories,
                    isLoading = true
                )
            }
        }
        .catch { emit(HomeState(categories = staticCategories, error = it.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeState(categories = staticCategories, isLoading = true)
        )

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            // Reactive pipeline handles loading automatically on subscription.
            // LoadHomeData is retained as a pull-to-refresh hook; wire a _refreshSignal
            // MutableStateFlow here when a refresh UI is added.
            is HomeIntent.LoadHomeData -> Unit
            is HomeIntent.OnBannerClick -> viewModelScope.launch {
                setEffect(HomeEffect.NavigateToDetails(intent.bannerId))
            }
            is HomeIntent.OnSearchClick -> viewModelScope.launch {
                setEffect(HomeEffect.NavigateToGlobalSearch)
            }
        }
    }

    companion object {
        // Companion so staticCategories is available both in the super() call (before
        // instance fields are initialised) and inside the uiState pipeline.
        val staticCategories: ImmutableList<HomeCategory> = persistentListOf(
            HomeCategory("Flights", TravelMonkIcons.Flight),
            HomeCategory("Hotels", TravelMonkIcons.Hotel),
            HomeCategory("Tours", TravelMonkIcons.Explore),
            HomeCategory("Yoga", TravelMonkIcons.SelfImprovement)
        )
    }
}
