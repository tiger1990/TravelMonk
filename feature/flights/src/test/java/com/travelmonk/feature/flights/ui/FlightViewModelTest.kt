package com.travelmonk.feature.flights.ui

import app.cash.turbine.test
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.testing.MainDispatcherRule
import com.travelmonk.feature.flights.domain.usecase.SearchFlightsUseCase
import com.travelmonk.feature.flights.fakes.FakeFlightRepository
import com.travelmonk.feature.flights.fixtures.FlightFixtures
import com.travelmonk.feature.flights.mvi.FlightEffect
import com.travelmonk.feature.flights.mvi.FlightIntent
import com.travelmonk.feature.flights.mvi.TripType
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FlightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeFlightRepository
    private lateinit var viewModel: FlightViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeFlightRepository()
        viewModel = FlightViewModel(SearchFlightsUseCase(fakeRepository))
    }

    @Test
    fun `searchFlights success updates state and emits NavigateToResults effect`() = runTest {
        fakeRepository.result = DataResult.Success(FlightFixtures.sampleFlights)

        viewModel.effect.test {
            viewModel.onIntent(FlightIntent.SearchFlights)

            val state = viewModel.uiState.value
            assertNull(state.error)
            assertEquals(FlightFixtures.sampleFlights, state.flights)

            val effect = awaitItem()
            assertTrue(effect is FlightEffect.NavigateToResults)
            assertEquals("San Francisco", (effect as FlightEffect.NavigateToResults).from)
            assertEquals("New York", effect.to)
        }
    }

    @Test
    fun `searchFlights error sets error state and emits ShowError effect`() = runTest {
        val error = RuntimeException("Network error")
        fakeRepository.result = DataResult.Error(error)

        viewModel.effect.test {
            viewModel.onIntent(FlightIntent.SearchFlights)

            val state = viewModel.uiState.value
            assertNotNull(state.error)
            assertEquals("Network error", state.error)
            assertTrue(state.flights.isEmpty())

            val effect = awaitItem()
            assertTrue(effect is FlightEffect.ShowError)
            assertEquals("Network error", (effect as FlightEffect.ShowError).message)
        }
    }

    @Test
    fun `swapCities swaps fromCity and toCity in state`() = runTest {
        viewModel.onIntent(FlightIntent.SwapCities(from = "San Francisco", to = "New York"))

        val state = viewModel.uiState.value
        assertEquals("New York", state.fromCity)
        assertEquals("San Francisco", state.toCity)
    }

    @Test
    fun `changeTripType updates tripType in state`() = runTest {
        viewModel.onIntent(FlightIntent.ChangeTripType(TripType.ROUND_TRIP))

        assertEquals(TripType.ROUND_TRIP, viewModel.uiState.value.tripType)
    }
}
