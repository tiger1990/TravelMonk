package com.travelmonk.feature.bookings.domain.model

import com.travelmonk.core.model.BookingType

data class BookingItem(
    val id: String,
    val type: BookingType,
    val title: String,
    val date: String,
    val status: String,
    val price: String
)
