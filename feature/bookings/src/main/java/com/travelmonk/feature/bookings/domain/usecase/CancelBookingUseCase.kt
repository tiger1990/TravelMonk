package com.travelmonk.feature.bookings.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.bookings.domain.repository.BookingRepository
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String): DataResult<Unit> =
        repository.cancelBooking(bookingId)
}
