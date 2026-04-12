package com.travelmonk.feature.bookings.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.feature.bookings.domain.repository.BookingRepository
import com.travelmonk.feature.bookings.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : BaseViewModel<BookingState, BookingIntent, BookingEffect>() {

    override fun createInitialState(): BookingState = BookingState()

    init {
        loadBookings()
    }

    override fun handleIntent(intent: BookingIntent) {
        when (intent) {
            is BookingIntent.LoadBookings -> loadBookings()
            is BookingIntent.CancelBooking -> {
                viewModelScope.launch {
                    try {
                        bookingRepository.cancelBooking(intent.id)
                        setEffect(BookingEffect.ShowMessage("Booking cancelled"))
                        loadBookings()
                    } catch (e: Exception) {
                        setEffect(BookingEffect.ShowMessage("Failed to cancel booking"))
                    }
                }
            }
        }
    }

    private fun loadBookings() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            try {
                val domainBookings = bookingRepository.getBookings()
                val uiBookings = domainBookings.map {
                    BookingItem(
                        it.id,
                        it.type,
                        it.title,
                        it.date,
                        it.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        it.price
                    )
                }
                setState { copy(bookings = uiBookings) }
            } catch (e: Exception) {
                // Handle error
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }
}
