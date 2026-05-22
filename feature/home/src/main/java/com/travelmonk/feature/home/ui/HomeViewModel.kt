package com.travelmonk.feature.home.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.home.domain.usecase.GetHomeBannersUseCase
import com.travelmonk.feature.home.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeBannersUseCase: GetHomeBannersUseCase
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>(
    HomeState(
        categories = listOf(
            HomeCategory("Flights", TravelMonkIcons.Flight),
            HomeCategory("Hotels", TravelMonkIcons.Hotel),
            HomeCategory("Tours", TravelMonkIcons.Explore),
            HomeCategory("Yoga", TravelMonkIcons.SelfImprovement)
        )
    )
) {

    override suspend fun initialDataLoad() {
        loadHomeData()
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadHomeData -> loadHomeData()
            is HomeIntent.OnBannerClick -> {
                viewModelScope.launch {
                    setEffect(HomeEffect.NavigateToDetails(intent.bannerId))
                }
            }
            is HomeIntent.OnSearchClick -> {
                viewModelScope.launch {
                    setEffect(HomeEffect.NavigateToGlobalSearch)
                }
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            getHomeBannersUseCase().collect { result ->
                when (result) {
                    is DataResult.Success -> setState { copy(banners = result.data, isLoading = false) }
                    is DataResult.Error -> setState { copy(isLoading = false, error = result.exception.message) }
                    is DataResult.Loading -> Unit
                }
            }
        }
    }
}
