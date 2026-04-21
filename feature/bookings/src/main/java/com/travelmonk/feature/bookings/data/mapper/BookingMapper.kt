package com.travelmonk.feature.bookings.data.mapper

import com.travelmonk.core.model.Booking
import com.travelmonk.core.model.BookingStatus
import com.travelmonk.core.model.BookingType
import com.travelmonk.feature.bookings.data.api.dto.BookingDto

/**
 * Maps [BookingDto] (network layer) to [Booking] (domain layer).
 * [type] and [status] are raw API strings; unknown values fall back to [BookingType.FLIGHT]
 * and [BookingStatus.PENDING] respectively.
 * Enhance field mappings here when real backend is integrated.
 */
fun BookingDto.toDomain(): Booking = Booking(
    id = id,
    type = BookingType.entries.firstOrNull { it.name == type.uppercase() } ?: BookingType.FLIGHT,
    title = title,
    date = date,
    status = BookingStatus.entries.firstOrNull { it.name == status.uppercase() } ?: BookingStatus.PENDING,
    price = price
)
