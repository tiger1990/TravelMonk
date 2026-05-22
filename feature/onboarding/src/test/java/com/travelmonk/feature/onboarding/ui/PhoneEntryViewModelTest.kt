package com.travelmonk.feature.onboarding.ui

import app.cash.turbine.test
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.common.ui.UiText
import com.travelmonk.core.testing.MainDispatcherRule
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.domain.usecase.SendOtpUseCase
import com.travelmonk.feature.onboarding.mvi.PhoneEntryEffect
import com.travelmonk.feature.onboarding.mvi.PhoneEntryIntent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PhoneEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sendOtpUseCase: SendOtpUseCase = mockk()
    private lateinit var viewModel: PhoneEntryViewModel

    @Before
    fun setup() {
        viewModel = PhoneEntryViewModel(sendOtpUseCase)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun `submit with blank phone sets invalid phone error`() = runTest {
        viewModel.onIntent(PhoneEntryIntent.Submit)

        assertEquals(UiText.Res(R.string.feature_onboarding_error_invalid_phone), viewModel.uiState.value.error)
    }

    @Test
    fun `submit without plus prefix sets invalid phone error`() = runTest {
        viewModel.onIntent(PhoneEntryIntent.PhoneChanged("9876543210"))
        viewModel.onIntent(PhoneEntryIntent.Submit)

        assertEquals(UiText.Res(R.string.feature_onboarding_error_invalid_phone), viewModel.uiState.value.error)
    }

    @Test
    fun `submit with too few digits sets invalid phone error`() = runTest {
        viewModel.onIntent(PhoneEntryIntent.PhoneChanged("+123"))
        viewModel.onIntent(PhoneEntryIntent.Submit)

        assertEquals(UiText.Res(R.string.feature_onboarding_error_invalid_phone), viewModel.uiState.value.error)
    }

    @Test
    fun `submit with too many digits sets invalid phone error`() = runTest {
        viewModel.onIntent(PhoneEntryIntent.PhoneChanged("+1234567890123456")) // 16 digits
        viewModel.onIntent(PhoneEntryIntent.Submit)

        assertEquals(UiText.Res(R.string.feature_onboarding_error_invalid_phone), viewModel.uiState.value.error)
    }

    @Test
    fun `phoneChanged clears existing error`() = runTest {
        viewModel.onIntent(PhoneEntryIntent.Submit) // trigger error
        assertTrue(viewModel.uiState.value.error != null)

        viewModel.onIntent(PhoneEntryIntent.PhoneChanged("+919876543210"))

        assertNull(viewModel.uiState.value.error)
    }

    // ── Success path ──────────────────────────────────────────────────────────

    @Test
    fun `valid phone on success emits NavigateToOtp effect`() = runTest {
        val phone = "+919876543210"
        coEvery { sendOtpUseCase(phone) } returns DataResult.Success(Unit)

        viewModel.effect.test {
            viewModel.onIntent(PhoneEntryIntent.PhoneChanged(phone))
            viewModel.onIntent(PhoneEntryIntent.Submit)

            assertEquals(PhoneEntryEffect.NavigateToOtp(phone), awaitItem())
        }
    }

    @Test
    fun `valid phone on success clears loading and error`() = runTest {
        val phone = "+919876543210"
        coEvery { sendOtpUseCase(phone) } returns DataResult.Success(Unit)

        viewModel.onIntent(PhoneEntryIntent.PhoneChanged(phone))
        viewModel.onIntent(PhoneEntryIntent.Submit)

        val state = viewModel.uiState.value
        assertTrue(!state.isLoading)
        assertNull(state.error)
    }

    // ── Error path ────────────────────────────────────────────────────────────

    @Test
    fun `api error with message shows raw UiText`() = runTest {
        val phone = "+919876543210"
        coEvery { sendOtpUseCase(phone) } returns DataResult.Error(Exception("Server down"), "Server down")

        viewModel.onIntent(PhoneEntryIntent.PhoneChanged(phone))
        viewModel.onIntent(PhoneEntryIntent.Submit)

        assertEquals(UiText.Raw("Server down"), viewModel.uiState.value.error)
    }

    @Test
    fun `api error without message shows fallback Res UiText`() = runTest {
        val phone = "+919876543210"
        coEvery { sendOtpUseCase(phone) } returns DataResult.Error(Exception("fail"), null)

        viewModel.onIntent(PhoneEntryIntent.PhoneChanged(phone))
        viewModel.onIntent(PhoneEntryIntent.Submit)

        assertEquals(UiText.Res(R.string.feature_onboarding_error_send_otp_failed), viewModel.uiState.value.error)
    }
}
