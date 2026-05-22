package com.travelmonk.feature.transport.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.transportapi.TransportTab

data class TransportState(
    val selectedTab: TransportTab = TransportTab.FLIGHTS,
    val isLoading: Boolean = false
) : UiState

sealed class TransportIntent : UiIntent {
    data class SelectTab(val tab: TransportTab) : TransportIntent()
}

sealed class TransportEffect : UiEffect
