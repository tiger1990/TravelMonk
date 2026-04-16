package com.travelmonk.feature.bookings.domain.usecase

import com.travelmonk.feature.bookings.domain.repository.BookingRepository
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String) =
        repository.cancelBooking(bookingId)
}
