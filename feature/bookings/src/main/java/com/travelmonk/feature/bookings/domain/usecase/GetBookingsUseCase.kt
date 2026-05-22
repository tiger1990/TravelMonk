package com.travelmonk.feature.bookings.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.model.Booking
import com.travelmonk.feature.bookings.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookingsUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    operator fun invoke(): Flow<DataResult<List<Booking>>> = repository.getBookings()
}
