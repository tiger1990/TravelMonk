package com.travelmonk.feature.bookings.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.model.Booking
import com.travelmonk.feature.bookings.data.api.dto.BookingDto
import com.travelmonk.feature.bookings.data.mapper.toDomain
import com.travelmonk.feature.bookings.domain.repository.BookingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookingRepositoryImpl @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BookingRepository {
    override suspend fun getBookings(): DataResult<List<Booking>> =
        withContext(ioDispatcher) {
            // TODO: Replace with real API call when backend is integrated:
            // DataResult.Success(bookingsApi.getBookings().map { it.toDomain() })
            DataResult.Success(fakeBookings())
        }

    private fun fakeBookings(): List<Booking> = listOf(
        BookingDto("1", "FLIGHT", "SFO to JFK", "Oct 24, 2024", "CONFIRMED", "$450").toDomain(),
        BookingDto("2", "HOTEL", "Grand Hyatt Paris", "Oct 25-30, 2024", "UPCOMING", "$1200").toDomain(),
        BookingDto("3", "SERVICE", "Maid Service - Paris", "Oct 26, 2024", "PENDING", "$30").toDomain(),
        BookingDto("4", "PACKAGE", "Bali Paradise Tour", "Nov 12, 2024", "CONFIRMED", "$499").toDomain()
    )

    override suspend fun cancelBooking(bookingId: String): DataResult<Unit> =
        withContext(ioDispatcher) {
            // TODO: Replace with real API call when backend is integrated:
            // DataResult.Success(bookingsApi.cancelBooking(bookingId))
            DataResult.Success(Unit)
        }
}
