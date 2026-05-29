package com.travelmonk.feature.bookings.ui

import androidx.lifecycle.viewModelScope
import com.travelmonk.core.common.mvi.BaseViewModel
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.bookings.domain.model.BookingItem
import com.travelmonk.feature.bookings.domain.usecase.CancelBookingUseCase
import com.travelmonk.feature.bookings.domain.usecase.GetBookingsUseCase
import com.travelmonk.feature.bookings.mvi.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase
) : BaseViewModel<BookingState, BookingIntent, BookingEffect>(BookingState()) {

    // G5: tracks the active cancel coroutine so rapid double-taps are de-duplicated
    private var cancelJob: Job? = null

    // Integer counter used purely as a refresh trigger. Each increment causes
    // flatMapLatest to cancel the in-flight stream and subscribe to getBookingsUseCase()
    // fresh — initial load (value = 0) and manual refreshes share the same mechanism.
    private val _refreshSignal = MutableStateFlow(0)

    // Reactive pipeline using flatMapLatest():
    // flatMapLatest cancels the previous getBookingsUseCase() stream on every new
    // _refreshSignal emission, guaranteeing no stale response reaches the UI after a
    // cancel + refresh cycle.
    //
    // Pipeline: drop(1) skips the MutableStateFlow's initial 0 so debounce never fires on
    // first subscription. The INNER onStart re-injects 0 immediately — initial load is instant.
    // debounce(300) only affects manual refresh signals (value ≥ 1): rapid pull-to-refresh
    // taps are collapsed and only the last emission within 300 ms triggers a re-fetch.
    //
    // Sequence for first load:
    //   MutableStateFlow(0) → drop(1) swallows it → debounce sees nothing →
    //   onStart emits 0 → flatMapLatest starts immediately.
    //
    // Sequence for manual refresh:
    //   _refreshSignal++ (value = 1) → drop(1) lets it through → debounce(300) waits →
    //   flatMapLatest cancels previous stream and re-fetches.
    //
    // NOTE: Do NOT add an outer .onStart { emit(BookingState(isLoading)) } after flatMapLatest.
    // stateIn's initialValue already handles the initial loading frame. An outer onStart
    // would re-fire on every upstream restart (user returns after 5 s), replacing the cached
    // success state with a loading flash — visible flicker.
    //
    // Room migration: unchanged — _refreshSignal remains as a pull-to-refresh escape hatch.
    override val uiState: StateFlow<BookingState> = _refreshSignal
        .drop(1)
        .debounce(300)
        .onStart { emit(0) }
        .flatMapLatest {
            getBookingsUseCase()
                .map { result ->
                    when (result) {
                        is DataResult.Success -> {
                            val uiBookings = result.data.map { booking ->
                                BookingItem(
                                    id = booking.id,
                                    type = booking.type,
                                    title = booking.title,
                                    date = booking.date,
                                    // Normalise status enum name to title-case for display.
                                    status = booking.status.name
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    price = booking.price
                                )
                            }
                            BookingState(bookings = uiBookings.toPersistentList())
                        }
                        is DataResult.Error   -> BookingState(error = result.exception.message)
                        is DataResult.Loading -> BookingState(isLoading = true)
                    }
                }
        }
        .catch { emit(BookingState(error = it.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BookingState(isLoading = true)
        )

    override fun handleIntent(intent: BookingIntent) {
        when (intent) {
            // Increment signal → flatMapLatest cancels current stream and restarts it.
            // Used for pull-to-refresh and retry-on-error.
            is BookingIntent.LoadBookings -> _refreshSignal.value++

            is BookingIntent.CancelBooking -> {
                // G5: cancel any in-flight cancel coroutine before starting a new one —
                // prevents rapid double-tap from firing multiple effects + double-incrementing _refreshSignal.
                cancelJob?.cancel()
                cancelJob = viewModelScope.launch {
                    when (cancelBookingUseCase(intent.id)) {
                        is DataResult.Success -> {
                            setEffect(BookingEffect.ShowMessage("Booking cancelled"))
                            // Re-fetch list by incrementing the signal — same mechanism
                            // as LoadBookings but triggered programmatically post-cancel.
                            _refreshSignal.value++
                        }
                        is DataResult.Error   -> setEffect(
                            BookingEffect.ShowMessage("Failed to cancel booking")
                        )
                        // suspend use case — Loading is not a terminal value for a mutation call.
                        is DataResult.Loading -> Unit
                    }
                }
            }
        }
    }
}
