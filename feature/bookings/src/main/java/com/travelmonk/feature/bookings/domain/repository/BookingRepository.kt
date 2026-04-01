package com.travelmonk.feature.bookings.domain.repository

import com.travelmonk.core.model.Booking

interface BookingRepository {
    suspend fun getBookings(): List<Booking>
    suspend fun cancelBooking(bookingId: String)
}
