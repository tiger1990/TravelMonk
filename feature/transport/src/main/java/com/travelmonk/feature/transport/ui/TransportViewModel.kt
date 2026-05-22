package com.travelmonk.feature.transport.ui

import androidx.lifecycle.SavedStateHandle
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.feature.transport.mvi.*
import com.travelmonk.feature.transportapi.TransportTab
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransportViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<TransportState, TransportIntent, TransportEffect>(
    TransportState(
        selectedTab = savedStateHandle.get<String>(KEY_SELECTED_TAB)
            ?.let { name -> TransportTab.entries.firstOrNull { it.name == name } }
            ?: TransportTab.FLIGHTS
    )
) {

    override fun handleIntent(intent: TransportIntent) {
        when (intent) {
            is TransportIntent.SelectTab -> {
                setState { copy(selectedTab = intent.tab) }
                savedStateHandle[KEY_SELECTED_TAB] = intent.tab.name
            }
        }
    }

    companion object {
        private const val KEY_SELECTED_TAB = "selected_tab"
    }
}