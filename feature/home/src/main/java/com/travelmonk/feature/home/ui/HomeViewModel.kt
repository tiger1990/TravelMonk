package com.travelmonk.feature.home.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.feature.home.domain.repository.HomeRepository
import com.travelmonk.feature.home.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>() {

    override fun createInitialState(): HomeState = HomeState()

    init {
        onIntent(HomeIntent.LoadHomeData)
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
            setState { copy(isLoading = true) }
            try {
                val banners = homeRepository.getHomeBanners()
                setState { copy(banners = banners, isLoading = false) }
            } catch (e: Exception) {
                setState { copy(isLoading = false, error = e.message) }
            }
        }
    }
}
