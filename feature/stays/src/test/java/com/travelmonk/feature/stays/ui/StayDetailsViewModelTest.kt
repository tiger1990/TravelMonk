package com.travelmonk.feature.stays.ui

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.testing.MainDispatcherRule
import com.travelmonk.feature.stays.domain.model.Stay
import com.travelmonk.feature.stays.domain.usecase.GetStayDetailsUseCase
import com.travelmonk.feature.stays.mvi.StayDetailsIntent
import com.travelmonk.feature.stays.mvi.StayDetailsState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

private const val STAY_ID = "stay-42"

class StayDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getStayDetailsUseCase: GetStayDetailsUseCase = mockk()

    private val sampleStay = Stay(STAY_ID, "Beach Villa", "Goa", "$200", "4.8", "img")

    private fun buildViewModel() = StayDetailsViewModel(STAY_ID, getStayDetailsUseCase)

    // StateFlow conflates the transient loading frame under the eager test dispatcher, so we assert
    // the settled (non-loading) state rather than a specific intermediate emission.
    private suspend fun ReceiveTurbine<StayDetailsState>.awaitSettled(): StayDetailsState {
        var item = awaitItem()
        while (item.isLoading) item = awaitItem()
        return item
    }

    @Test
    fun `init seeds the load from the assisted stayId and emits success`() = runTest {
        every { getStayDetailsUseCase(STAY_ID) } returns
            flowOf(DataResult.Loading, DataResult.Success(sampleStay))

        val vm = buildViewModel()

        vm.uiState.test {
            val settled = awaitSettled()
            assertEquals(sampleStay, settled.stay)
            assertFalse(settled.isLoading)
        }
    }

    @Test
    fun `init load error surfaces error state`() = runTest {
        every { getStayDetailsUseCase(STAY_ID) } returns
            flowOf(DataResult.Error(RuntimeException("boom"), "boom"))

        val vm = buildViewModel()

        vm.uiState.test {
            assertEquals("boom", awaitSettled().error)
        }
    }

    @Test
    fun `Retry re-emits the same stayId and reloads after an error`() = runTest {
        every { getStayDetailsUseCase(STAY_ID) } returnsMany listOf(
            flowOf(DataResult.Error(RuntimeException("boom"), "boom")),
            flowOf(DataResult.Success(sampleStay)),
        )

        val vm = buildViewModel()

        vm.uiState.test {
            assertEquals("boom", awaitSettled().error)   // initial load fails

            vm.onIntent(StayDetailsIntent.Retry)         // re-emit the same id

            assertEquals(sampleStay, awaitSettled().stay) // reload succeeds
        }
    }
}
