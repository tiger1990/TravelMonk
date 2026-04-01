package com.travelmonk.feature.transport.ui

import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.feature.transport.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransportViewModel @Inject constructor() : BaseViewModel<TransportState, TransportIntent, TransportEffect>() {
    override fun createInitialState(): TransportState = TransportState()

    override fun handleIntent(intent: TransportIntent) {
        when (intent) {
            is TransportIntent.SelectTab -> setState { copy(selectedTab = intent.tab) }
        }
    }
}