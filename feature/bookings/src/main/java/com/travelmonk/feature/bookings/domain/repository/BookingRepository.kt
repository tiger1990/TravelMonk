package com.travelmonk.feature.bookings.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.model.Booking
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun getBookings(): Flow<DataResult<List<Booking>>>
    suspend fun cancelBooking(bookingId: String): DataResult<Unit>
}
