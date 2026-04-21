package com.travelmonk.feature.bookings.data.api

import com.travelmonk.feature.bookings.data.api.dto.BookingDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface BookingsApi {
    @GET("bookings")
    suspend fun getBookings(): List<BookingDto>

    @DELETE("bookings/{id}")
    suspend fun cancelBooking(@Path("id") bookingId: String)
}
