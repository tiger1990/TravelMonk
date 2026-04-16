package com.travelmonk.feature.bookings.data.api

import com.travelmonk.core.model.Booking
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface BookingsApi {
    @GET("bookings")
    suspend fun getBookings(): List<Booking>  // TODO: replace with BookingDto when real API is integrated

    @DELETE("bookings/{id}")
    suspend fun cancelBooking(@Path("id") bookingId: String)
}
