package com.travelmonk.feature.stays.ui

import app.cash.turbine.test
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.testing.MainDispatcherRule
import com.travelmonk.feature.stays.domain.model.Stay
import com.travelmonk.feature.stays.domain.usecase.SearchStaysUseCase
import com.travelmonk.feature.stays.mvi.StayResultsEffect
import com.travelmonk.feature.stays.mvi.StayResultsIntent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val LOCATION = "Goa"

class StayResultsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchStaysUseCase: SearchStaysUseCase = mockk()

    private val sampleStays = listOf(
        Stay("1", "Beach Villa", LOCATION, "$200", "4.8", "img1"),
        Stay("2", "City Loft", LOCATION, "$120", "4.5", "img2"),
    )

    // Constructed directly — the @AssistedInject migration means no fake SavedStateHandle is needed.
    private fun buildViewModel() = StayResultsViewModel(LOCATION, searchStaysUseCase)

    @Test
    fun `assisted location seeds initial state`() = runTest {
        coEvery { searchStaysUseCase(LOCATION) } returns DataResult.Success(emptyList())

        val vm = buildViewModel()

        assertEquals(LOCATION, vm.uiState.value.location)
    }

    @Test
    fun `init load success populates stays and clears loading`() = runTest {
        coEvery { searchStaysUseCase(LOCATION) } returns DataResult.Success(sampleStays)

        val vm = buildViewModel()

        val state = vm.uiState.value
        assertEquals(sampleStays, state.stays)
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `init load error sets error state and emits ShowError effect`() = runTest {
        coEvery { searchStaysUseCase(LOCATION) } returns
            DataResult.Error(RuntimeException("boom"), "boom")

        val vm = buildViewModel()

        // The effect is emitted during init's eager load; the BUFFERED channel holds it.
        vm.effect.test {
            val effect = awaitItem()
            assertTrue(effect is StayResultsEffect.ShowError)
            assertEquals("boom", (effect as StayResultsEffect.ShowError).message)
        }
        assertEquals("boom", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `Retry reloads stays after an initial error`() = runTest {
        coEvery { searchStaysUseCase(LOCATION) } returnsMany listOf(
            DataResult.Error(RuntimeException("boom"), "boom"),
            DataResult.Success(sampleStays),
        )

        val vm = buildViewModel()
        assertEquals("boom", vm.uiState.value.error)

        vm.onIntent(StayResultsIntent.Retry)

        assertEquals(sampleStays, vm.uiState.value.stays)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `SelectStay emits NavigateToDetail with the stay id`() = runTest {
        coEvery { searchStaysUseCase(LOCATION) } returns DataResult.Success(sampleStays)
        val vm = buildViewModel()

        vm.effect.test {
            vm.onIntent(StayResultsIntent.SelectStay(sampleStays.first()))

            val effect = awaitItem()
            assertTrue(effect is StayResultsEffect.NavigateToDetail)
            assertEquals("1", (effect as StayResultsEffect.NavigateToDetail).stayId)
        }
    }
}
