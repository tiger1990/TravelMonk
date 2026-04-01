package com.travelmonk.feature.services.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.feature.services.domain.repository.ServiceRepository
import com.travelmonk.feature.services.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
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
            try {
                val domainServices = serviceRepository.getServices()
                // In a more complex app, we'd map domain to UI models here if they differ
                // For now, the UI uses its own TravelService which we'll keep for simplicity
                // but usually, we'd sync them.
            } catch (e: Exception) {
                // Handle error
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }
}
