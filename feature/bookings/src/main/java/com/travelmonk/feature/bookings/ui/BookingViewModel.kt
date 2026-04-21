package com.travelmonk.feature.bookings.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.bookings.domain.model.BookingItem
import com.travelmonk.feature.bookings.domain.usecase.CancelBookingUseCase
import com.travelmonk.feature.bookings.domain.usecase.GetBookingsUseCase
import com.travelmonk.feature.bookings.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase
) : BaseViewModel<BookingState, BookingIntent, BookingEffect>() {

    override fun createInitialState(): BookingState = BookingState()

    override suspend fun initialDataLoad() {
        loadBookings()
    }

    override fun handleIntent(intent: BookingIntent) {
        when (intent) {
            is BookingIntent.LoadBookings -> loadBookings()
            is BookingIntent.CancelBooking -> {
                viewModelScope.launch {
                    when (cancelBookingUseCase(intent.id)) {
                        is DataResult.Success -> {
                            setEffect(BookingEffect.ShowMessage("Booking cancelled"))
                            loadBookings()
                        }
                        is DataResult.Error -> setEffect(BookingEffect.ShowMessage("Failed to cancel booking"))
                        is DataResult.Loading -> Unit
                    }
                }
            }
        }
    }

    private fun loadBookings() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            when (val result = getBookingsUseCase()) {
                is DataResult.Success -> {
                    val uiBookings = result.data.map {
                        BookingItem(
                            it.id,
                            it.type,
                            it.title,
                            it.date,
                            it.status.name.lowercase().replaceFirstChar { c -> c.uppercase() },
                            it.price
                        )
                    }
                    setState { copy(bookings = uiBookings, isLoading = false) }
                }
                is DataResult.Error -> {
                    setState { copy(isLoading = false, error = result.exception.message) }
                    setEffect(BookingEffect.ShowMessage(result.exception.message ?: "Failed to load bookings"))
                }
                is DataResult.Loading -> Unit
            }
        }
    }
}
