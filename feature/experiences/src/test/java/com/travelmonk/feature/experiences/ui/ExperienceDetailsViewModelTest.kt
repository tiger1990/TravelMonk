package com.travelmonk.feature.experiences.ui

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.testing.MainDispatcherRule
import com.travelmonk.feature.experiences.domain.model.Experience
import com.travelmonk.feature.experiences.domain.model.ExperienceCategory
import com.travelmonk.feature.experiences.domain.usecase.GetExperienceDetailsUseCase
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsIntent
import com.travelmonk.feature.experiences.mvi.ExperienceDetailsState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

private const val EXPERIENCE_ID = "exp-7"

class ExperienceDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getExperienceDetailsUseCase: GetExperienceDetailsUseCase = mockk()

    private val sampleExperience = Experience(
        id = EXPERIENCE_ID,
        title = "Sunset Kayak",
        description = "Paddle the bay at dusk",
        price = "$80",
        rating = 4.9,
        imageUrl = "img",
        category = ExperienceCategory.PACKAGES,
    )

    private fun buildViewModel() = ExperienceDetailsViewModel(EXPERIENCE_ID, getExperienceDetailsUseCase)

    // StateFlow conflates the transient loading frame under the eager test dispatcher, so we assert
    // the settled (non-loading) state rather than a specific intermediate emission.
    private suspend fun ReceiveTurbine<ExperienceDetailsState>.awaitSettled(): ExperienceDetailsState {
        var item = awaitItem()
        while (item.isLoading) item = awaitItem()
        return item
    }

    @Test
    fun `init seeds the load from the assisted experienceId and emits success`() = runTest {
        every { getExperienceDetailsUseCase(EXPERIENCE_ID) } returns
            flowOf(DataResult.Loading, DataResult.Success(sampleExperience))

        val vm = buildViewModel()

        vm.uiState.test {
            val settled = awaitSettled()
            assertEquals(sampleExperience, settled.experience)
            assertFalse(settled.isLoading)
        }
    }

    @Test
    fun `init load error surfaces error state`() = runTest {
        every { getExperienceDetailsUseCase(EXPERIENCE_ID) } returns
            flowOf(DataResult.Error(RuntimeException("boom"), "boom"))

        val vm = buildViewModel()

        vm.uiState.test {
            assertEquals("boom", awaitSettled().error)
        }
    }

    @Test
    fun `Retry re-emits the same experienceId and reloads after an error`() = runTest {
        every { getExperienceDetailsUseCase(EXPERIENCE_ID) } returnsMany listOf(
            flowOf(DataResult.Error(RuntimeException("boom"), "boom")),
            flowOf(DataResult.Success(sampleExperience)),
        )

        val vm = buildViewModel()

        vm.uiState.test {
            assertEquals("boom", awaitSettled().error)                // initial load fails

            vm.onIntent(ExperienceDetailsIntent.Retry)                // re-emit the same id

            assertEquals(sampleExperience, awaitSettled().experience) // reload succeeds
        }
    }
}
