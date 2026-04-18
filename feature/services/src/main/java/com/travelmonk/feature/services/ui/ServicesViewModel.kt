package com.travelmonk.feature.services.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.services.domain.usecase.GetServicesUseCase
import com.travelmonk.feature.services.mvi.ServicesEffect
import com.travelmonk.feature.services.mvi.ServicesIntent
import com.travelmonk.feature.services.mvi.ServicesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val getServicesUseCase: GetServicesUseCase
) : BaseViewModel<ServicesState, ServicesIntent, ServicesEffect>() {
    override fun createInitialState(): ServicesState = ServicesState()

    init {
        loadServices()
    }

    override fun handleIntent(intent: ServicesIntent) {
        when (intent) {
            is ServicesIntent.SelectService -> {
                setState { copy(selectedService = intent.service) }
                viewModelScope.launch {
                    setEffect(ServicesEffect.NavigateToBooking(intent.service))
                }
            }
        }
    }

    private fun loadServices() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (val result = getServicesUseCase()) {
                is DataResult.Success -> setState { copy(services = result.data, isLoading = false) }
                is DataResult.Error -> setState { copy(isLoading = false) }
                is DataResult.Loading -> Unit
            }
        }
    }
}
