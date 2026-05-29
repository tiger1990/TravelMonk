package com.travelmonk.feature.bookings.mvi

import androidx.compose.runtime.Immutable
import com.travelmonk.core.common.mvi.UiEffect
import com.travelmonk.core.common.mvi.UiIntent
import com.travelmonk.core.common.mvi.UiState
import com.travelmonk.feature.bookings.domain.model.BookingItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class BookingState(
    val bookings: ImmutableList<BookingItem> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface BookingIntent : UiIntent {
    data object LoadBookings : BookingIntent
    data class CancelBooking(val id: String) : BookingIntent
}

sealed interface BookingEffect : UiEffect {
    data class ShowMessage(val message: String) : BookingEffect
}
