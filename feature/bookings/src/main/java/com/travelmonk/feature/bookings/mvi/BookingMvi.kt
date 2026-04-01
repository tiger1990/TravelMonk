package com.travelmonk.feature.bookings.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState

data class BookingState(
    val bookings: List<BookingItem> = emptyList(),
    val isLoading: Boolean = false
) : UiState

data class BookingItem(
    val id: String,
    val type: BookingType,
    val title: String,
    val date: String,
    val status: String,
    val price: String
)

enum class BookingType { FLIGHT, BUS, TRAIN, HOTEL, PACKAGE, SERVICE }

sealed class BookingIntent : UiIntent {
    data object LoadBookings : BookingIntent()
    data class CancelBooking(val id: String) : BookingIntent()
}

sealed class BookingEffect : UiEffect {
    data class ShowMessage(val message: String) : BookingEffect()
}
