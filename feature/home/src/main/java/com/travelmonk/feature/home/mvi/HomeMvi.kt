package com.travelmonk.feature.home.mvi

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.home.domain.model.HomeBanner
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class HomeCategory(
    val label: String,
    @param:DrawableRes val icon: Int
)

@Immutable
data class HomeState(
    val banners: ImmutableList<HomeBanner> = persistentListOf(),
    val categories: ImmutableList<HomeCategory> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface HomeIntent : UiIntent {
    data object LoadHomeData : HomeIntent
    data class OnBannerClick(val bannerId: String) : HomeIntent
    data object OnSearchClick : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    data class NavigateToDetails(val id: String) : HomeEffect
    data object NavigateToGlobalSearch : HomeEffect
}
