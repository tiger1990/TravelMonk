package com.travelmonk.feature.bookings.mvi

import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.bookings.domain.model.BookingItem

data class BookingState(
    val bookings: List<BookingItem> = emptyList(),
    val isLoading: Boolean = false
) : UiState

sealed class BookingIntent : UiIntent {
    data object LoadBookings : BookingIntent()
    data class CancelBooking(val id: String) : BookingIntent()
}

sealed class BookingEffect : UiEffect {
    data class ShowMessage(val message: String) : BookingEffect()
}
