package com.travelmonk.feature.home.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.home.domain.model.HomeBanner

data class HomeState(
    val banners: List<HomeBanner> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed class HomeIntent : UiIntent {
    data object LoadHomeData : HomeIntent()
    data class OnBannerClick(val bannerId: String) : HomeIntent()
    data object OnSearchClick : HomeIntent()
}

sealed class HomeEffect : UiEffect {
    data class NavigateToDetails(val id: String) : HomeEffect()
    data object NavigateToGlobalSearch : HomeEffect()
}
