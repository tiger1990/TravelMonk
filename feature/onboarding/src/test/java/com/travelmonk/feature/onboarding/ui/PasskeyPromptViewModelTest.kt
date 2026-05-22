package com.travelmonk.feature.onboarding.ui

import app.cash.turbine.test
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.common.ui.UiText
import com.travelmonk.core.testing.MainDispatcherRule
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.data.local.SessionData
import com.travelmonk.feature.onboarding.data.local.UserSessionStore
import com.travelmonk.feature.onboarding.data.mock.PasskeyMockDataSource
import com.travelmonk.feature.onboarding.domain.repository.PasskeyRepository
import com.travelmonk.feature.onboarding.domain.usecase.PasskeyAuthUseCase
import com.travelmonk.feature.onboarding.domain.usecase.PasskeyRegistrationUseCase
import com.travelmonk.feature.onboarding.mvi.PasskeyPromptEffect
import com.travelmonk.feature.onboarding.mvi.PasskeyPromptIntent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val USER_ID = "usr_001"
private const val CHALLENGE_JSON = """{"challenge":"abc123"}"""
private const val ATTESTATION_JSON = """{"attestation":"def456"}"""
private const val ASSERTION_JSON = """{"assertion":"ghi789"}"""

class PasskeyPromptViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val passkeyRepository: PasskeyRepository = mockk()
    private val passkeyRegistrationUseCase: PasskeyRegistrationUseCase = mockk()
    private val passkeyAuthUseCase: PasskeyAuthUseCase = mockk()
    private val userSessionStore: UserSessionStore = mockk()
    private val mockDataSource: PasskeyMockDataSource = mockk {
        every { isEnabled } returns false
    }

    @Before
    fun setup() {
        every { userSessionStore.sessionFlow } returns MutableStateFlow(
            SessionData(userId = USER_ID, accessToken = "tok_abc")
        )
        coEvery { userSessionStore.markOnboardingComplete() } just Runs
    }

    private fun buildViewModel() = PasskeyPromptViewModel(
        passkeyRepository = passkeyRepository,
        passkeyRegistrationUseCase = passkeyRegistrationUseCase,
        passkeyAuthUseCase = passkeyAuthUseCase,
        userSessionStore = userSessionStore,
        mockDataSource = mockDataSource
    )

    // ── RegisterPasskey ───────────────────────────────────────────────────────

    @Test
    fun `RegisterPasskey success emits LaunchPasskeyRegistration effect`() = runTest {
        coEvery { passkeyRepository.beginRegistration(USER_ID) } returns
            DataResult.Success(CHALLENGE_JSON)

        val vm = buildViewModel()
        vm.effect.test {
            vm.onIntent(PasskeyPromptIntent.RegisterPasskey)
            assertEquals(PasskeyPromptEffect.LaunchPasskeyRegistration(CHALLENGE_JSON), awaitItem())
        }
    }

    @Test
    fun `RegisterPasskey error with message sets Raw error state`() = runTest {
        coEvery { passkeyRepository.beginRegistration(USER_ID) } returns
            DataResult.Error(Exception("Service unavailable"), "Service unavailable")

        val vm = buildViewModel()
        backgroundScope.launch { vm.uiState.collect {} } // activate stateIn so updates propagate
        vm.onIntent(PasskeyPromptIntent.RegisterPasskey)
        advanceUntilIdle()

        assertEquals(UiText.Raw("Service unavailable"), vm.uiState.value.error)
    }

    @Test
    fun `RegisterPasskey error without message sets fallback Res error`() = runTest {
        coEvery { passkeyRepository.beginRegistration(USER_ID) } returns
            DataResult.Error(Exception("fail"), null)

        val vm = buildViewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        vm.onIntent(PasskeyPromptIntent.RegisterPasskey)
        advanceUntilIdle()

        assertEquals(
            UiText.Res(R.string.feature_onboarding_error_passkey_registration_start),
            vm.uiState.value.error
        )
    }

    // ── SignInWithPasskey ─────────────────────────────────────────────────────

    @Test
    fun `SignInWithPasskey success emits LaunchPasskeyAuthentication effect`() = runTest {
        coEvery { passkeyRepository.beginAuthentication() } returns
            DataResult.Success(CHALLENGE_JSON)

        val vm = buildViewModel()
        vm.effect.test {
            vm.onIntent(PasskeyPromptIntent.SignInWithPasskey)
            assertEquals(PasskeyPromptEffect.LaunchPasskeyAuthentication(CHALLENGE_JSON), awaitItem())
        }
    }

    @Test
    fun `SignInWithPasskey error without message sets fallback Res error`() = runTest {
        coEvery { passkeyRepository.beginAuthentication() } returns
            DataResult.Error(Exception("fail"), null)

        val vm = buildViewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        vm.onIntent(PasskeyPromptIntent.SignInWithPasskey)
        advanceUntilIdle()

        assertEquals(
            UiText.Res(R.string.feature_onboarding_error_passkey_auth_start),
            vm.uiState.value.error
        )
    }

    // ── AttestationObtained ───────────────────────────────────────────────────

    @Test
    fun `AttestationObtained success calls markOnboardingComplete`() = runTest {
        coEvery { passkeyRegistrationUseCase(USER_ID, ATTESTATION_JSON) } returns
            DataResult.Success(mockk())

        val vm = buildViewModel()
        vm.onIntent(PasskeyPromptIntent.AttestationObtained(ATTESTATION_JSON))

        coVerify(exactly = 1) { userSessionStore.markOnboardingComplete() }
    }

    @Test
    fun `AttestationObtained success clears loading state`() = runTest {
        coEvery { passkeyRegistrationUseCase(USER_ID, ATTESTATION_JSON) } returns
            DataResult.Success(mockk())

        val vm = buildViewModel()
        vm.onIntent(PasskeyPromptIntent.AttestationObtained(ATTESTATION_JSON))

        assertNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `AttestationObtained error without message sets fallback Res error`() = runTest {
        coEvery { passkeyRegistrationUseCase(USER_ID, ATTESTATION_JSON) } returns
            DataResult.Error(Exception("fail"), null)

        val vm = buildViewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        vm.onIntent(PasskeyPromptIntent.AttestationObtained(ATTESTATION_JSON))
        advanceUntilIdle()

        assertEquals(
            UiText.Res(R.string.feature_onboarding_error_passkey_registration_failed),
            vm.uiState.value.error
        )
    }

    // ── AssertionObtained ─────────────────────────────────────────────────────

    @Test
    fun `AssertionObtained success calls markOnboardingComplete`() = runTest {
        coEvery { passkeyAuthUseCase(ASSERTION_JSON) } returns DataResult.Success(mockk())

        val vm = buildViewModel()
        vm.onIntent(PasskeyPromptIntent.AssertionObtained(ASSERTION_JSON))

        coVerify(exactly = 1) { userSessionStore.markOnboardingComplete() }
    }

    @Test
    fun `AssertionObtained error without message sets fallback Res error`() = runTest {
        coEvery { passkeyAuthUseCase(ASSERTION_JSON) } returns
            DataResult.Error(Exception("fail"), null)

        val vm = buildViewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        vm.onIntent(PasskeyPromptIntent.AssertionObtained(ASSERTION_JSON))
        advanceUntilIdle()

        assertEquals(
            UiText.Res(R.string.feature_onboarding_error_passkey_auth_failed),
            vm.uiState.value.error
        )
    }

    // ── Skip ──────────────────────────────────────────────────────────────────

    @Test
    fun `Skip calls markOnboardingComplete`() = runTest {
        val vm = buildViewModel()
        vm.onIntent(PasskeyPromptIntent.Skip)

        coVerify(exactly = 1) { userSessionStore.markOnboardingComplete() }
    }

    // ── Mock-enabled paths ────────────────────────────────────────────────────

    @Test
    fun `RegisterPasskey with mock enabled dispatches AttestationObtained without emitting effect`() = runTest {
        coEvery { passkeyRepository.beginRegistration(USER_ID) } returns DataResult.Success(CHALLENGE_JSON)
        coEvery { passkeyRegistrationUseCase(USER_ID, any()) } returns DataResult.Success(mockk())
        every { mockDataSource.isEnabled } returns true
        every { mockDataSource.attestationResponseJson() } returns ATTESTATION_JSON

        val vm = buildViewModel()
        vm.effect.test {
            vm.onIntent(PasskeyPromptIntent.RegisterPasskey)
            expectNoEvents()
        }
        coVerify { passkeyRegistrationUseCase(USER_ID, ATTESTATION_JSON) }
    }

    @Test
    fun `SignInWithPasskey with mock enabled dispatches AssertionObtained without emitting effect`() = runTest {
        coEvery { passkeyRepository.beginAuthentication() } returns DataResult.Success(CHALLENGE_JSON)
        coEvery { passkeyAuthUseCase(any()) } returns DataResult.Success(mockk())
        every { mockDataSource.isEnabled } returns true
        every { mockDataSource.assertionResponseJson() } returns ASSERTION_JSON

        val vm = buildViewModel()
        vm.effect.test {
            vm.onIntent(PasskeyPromptIntent.SignInWithPasskey)
            expectNoEvents()
        }
        coVerify { passkeyAuthUseCase(ASSERTION_JSON) }
    }
}