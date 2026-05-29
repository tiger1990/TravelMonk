package com.travelmonk.feature.transport.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.transportapi.TransportTab

@Immutable
data class TransportState(
    val selectedTab: TransportTab = TransportTab.FLIGHTS,
    val isLoading: Boolean = false
) : UiState

sealed interface TransportIntent : UiIntent {
    data class SelectTab(val tab: TransportTab) : TransportIntent
}

sealed interface TransportEffect : UiEffect
