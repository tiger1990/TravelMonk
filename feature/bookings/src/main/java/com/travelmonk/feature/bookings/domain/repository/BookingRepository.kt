package com.travelmonk.feature.bookings.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.model.Booking

interface BookingRepository {
    suspend fun getBookings(): DataResult<List<Booking>>
    suspend fun cancelBooking(bookingId: String): DataResult<Unit>
}
