package com.travelmonk.feature.bookings.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.model.Booking
import com.travelmonk.core.model.BookingStatus
import com.travelmonk.core.model.BookingType
import com.travelmonk.feature.bookings.domain.repository.BookingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BookingRepository {
    override suspend fun getBookings(): List<Booking> =
        withContext(ioDispatcher) {
            listOf(
                Booking("1", BookingType.FLIGHT, "SFO to JFK", "Oct 24, 2024", BookingStatus.CONFIRMED, "$450"),
                Booking("2", BookingType.HOTEL, "Grand Hyatt Paris", "Oct 25-30, 2024", BookingStatus.UPCOMING, "$1200"),
                Booking("3", BookingType.SERVICE, "Maid Service - Paris", "Oct 26, 2024", BookingStatus.PENDING, "$30"),
                Booking("4", BookingType.PACKAGE, "Bali Paradise Tour", "Nov 12, 2024", BookingStatus.CONFIRMED, "$499")
            )
        }

    override suspend fun cancelBooking(bookingId: String) =
        withContext(ioDispatcher) {
            // Implementation
        }
}
