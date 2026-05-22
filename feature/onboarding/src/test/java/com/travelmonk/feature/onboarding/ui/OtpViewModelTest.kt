package com.travelmonk.feature.onboarding.ui

import app.cash.turbine.test
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.common.ui.UiText
import com.travelmonk.core.testing.MainDispatcherRule
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.domain.usecase.SendOtpUseCase
import com.travelmonk.feature.onboarding.domain.usecase.VerifyOtpUseCase
import com.travelmonk.feature.onboarding.mvi.OtpEffect
import com.travelmonk.feature.onboarding.mvi.OtpIntent
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val PHONE = "+919876543210"
private const val VALID_OTP = "123456"

class OtpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val verifyOtpUseCase: VerifyOtpUseCase = mockk()
    private val sendOtpUseCase: SendOtpUseCase = mockk()

    private fun buildViewModel(savedCooldown: Int = 0): OtpViewModel {
        val savedState = androidx.lifecycle.SavedStateHandle(
            mapOf("phone" to PHONE, "resend_cooldown" to savedCooldown)
        )
        return OtpViewModel(savedState, verifyOtpUseCase, sendOtpUseCase)
    }

    // ── Digits-only guard ─────────────────────────────────────────────────────

    @Test
    fun `non-digit otp input is discarded`() = runTest {
        val vm = buildViewModel()
        vm.onIntent(OtpIntent.OtpChanged("abc123"))

        assertEquals("", vm.uiState.value.otp)
    }

    @Test
    fun `digit-only otp input is accepted`() = runTest {
        val vm = buildViewModel()
        vm.onIntent(OtpIntent.OtpChanged("123456"))

        assertEquals("123456", vm.uiState.value.otp)
    }

    // ── Verify OTP ────────────────────────────────────────────────────────────

    @Test
    fun `verify success emits NavigateToPasskeyPrompt effect`() = runTest {
        val vm = buildViewModel()
        coEvery { verifyOtpUseCase(PHONE, VALID_OTP) } returns DataResult.Success(mockk())

        vm.effect.test {
            vm.onIntent(OtpIntent.OtpChanged(VALID_OTP))
            vm.onIntent(OtpIntent.Submit)

            assertEquals(OtpEffect.NavigateToPasskeyPrompt, awaitItem())
        }
    }

    @Test
    fun `verify error with message shows raw UiText`() = runTest {
        val vm = buildViewModel()
        coEvery { verifyOtpUseCase(PHONE, VALID_OTP) } returns
            DataResult.Error(Exception("Invalid code"), "Invalid code")

        vm.onIntent(OtpIntent.OtpChanged(VALID_OTP))
        vm.onIntent(OtpIntent.Submit)

        assertEquals(UiText.Raw("Invalid code"), vm.uiState.value.error)
    }

    @Test
    fun `verify error without message shows fallback Res UiText`() = runTest {
        val vm = buildViewModel()
        coEvery { verifyOtpUseCase(PHONE, VALID_OTP) } returns DataResult.Error(Exception("fail"), null)

        vm.onIntent(OtpIntent.OtpChanged(VALID_OTP))
        vm.onIntent(OtpIntent.Submit)

        assertEquals(UiText.Res(R.string.feature_onboarding_error_otp_verification_failed), vm.uiState.value.error)
    }

    // ── Resend OTP ────────────────────────────────────────────────────────────

    @Test
    fun `resend success starts cooldown`() = runTest {
        val vm = buildViewModel()
        coEvery { sendOtpUseCase(PHONE) } returns DataResult.Success(Unit)

        vm.onIntent(OtpIntent.ResendOtp)

        assertTrue(vm.uiState.value.resendCooldownSeconds > 0)
    }

    @Test
    fun `resend error sets error state and does not start cooldown`() = runTest {
        val vm = buildViewModel()
        coEvery { sendOtpUseCase(PHONE) } returns DataResult.Error(Exception("fail"), null)

        vm.onIntent(OtpIntent.ResendOtp)

        assertEquals(UiText.Res(R.string.feature_onboarding_error_resend_failed), vm.uiState.value.error)
        assertEquals(0, vm.uiState.value.resendCooldownSeconds)
    }

    @Test
    fun `resend ignored when cooldown is active`() = runTest {
        val vm = buildViewModel()
        coEvery { sendOtpUseCase(PHONE) } returns DataResult.Success(Unit)

        vm.onIntent(OtpIntent.ResendOtp)
        val cooldownAfterFirst = vm.uiState.value.resendCooldownSeconds

        vm.onIntent(OtpIntent.ResendOtp) // should be ignored
        assertEquals(cooldownAfterFirst, vm.uiState.value.resendCooldownSeconds)
    }

    // ── Cooldown resume after process death ───────────────────────────────────

    @Test
    fun `saved cooldown is initialised from SavedStateHandle`() = runTest {
        val vm = buildViewModel(savedCooldown = 10)

        assertEquals(10, vm.uiState.value.resendCooldownSeconds)
    }

    @Test
    fun `cooldown ticks down to zero`() = runTest {
        val vm = buildViewModel(savedCooldown = 2)

        advanceTimeBy(3_000)

        assertEquals(0, vm.uiState.value.resendCooldownSeconds)
    }

    // ── Error cleared on new input ────────────────────────────────────────────

    @Test
    fun `otp change clears existing error`() = runTest {
        val vm = buildViewModel()
        coEvery { verifyOtpUseCase(PHONE, VALID_OTP) } returns DataResult.Error(Exception(), null)

        vm.onIntent(OtpIntent.OtpChanged(VALID_OTP))
        vm.onIntent(OtpIntent.Submit)
        assertTrue(vm.uiState.value.error != null)

        vm.onIntent(OtpIntent.OtpChanged("654321"))

        assertNull(vm.uiState.value.error)
    }
}
