package com.travelmonk.feature.onboarding.domain.usecase

import com.travelmonk.core.common.config.FeatureFlagSyncer
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.testing.MainDispatcherRule
import com.travelmonk.feature.onboarding.data.local.UserSessionStore
import com.travelmonk.feature.onboarding.domain.model.AuthToken
import com.travelmonk.feature.onboarding.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VerifyOtpUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val userSessionStore: UserSessionStore = mockk()
    private val featureFlagSyncer: FeatureFlagSyncer = mockk()

    private lateinit var useCase: VerifyOtpUseCase

    @Before
    fun setup() {
        useCase = VerifyOtpUseCase(authRepository, userSessionStore, featureFlagSyncer)
    }

    @Test
    fun `success - saveSession called with token`() = runTest {
        val token = AuthToken(
            accessToken = "access_abc",
            refreshToken = "refresh_xyz",
            userId = "usr_001",
            phoneNumber = "+919876543210"
        )
        coEvery { authRepository.verifyOtp(any(), any()) } returns DataResult.Success(token)
        coEvery { userSessionStore.saveSession(any()) } just runs
        coEvery { featureFlagSyncer.sync() } just runs

        useCase("+919876543210", "123456")

        coVerify(exactly = 1) { userSessionStore.saveSession(token) }
    }

    @Test
    fun `success - featureFlagSyncer sync called`() = runTest {
        val token = AuthToken("access_abc", "refresh_xyz", "usr_001", "+919876543210")
        coEvery { authRepository.verifyOtp(any(), any()) } returns DataResult.Success(token)
        coEvery { userSessionStore.saveSession(any()) } just runs
        coEvery { featureFlagSyncer.sync() } just runs

        useCase("+919876543210", "123456")

        coVerify(exactly = 1) { featureFlagSyncer.sync() }
    }

    @Test
    fun `success - returns token from repository`() = runTest {
        val token = AuthToken("access_abc", "refresh_xyz", "usr_001", "+919876543210")
        coEvery { authRepository.verifyOtp(any(), any()) } returns DataResult.Success(token)
        coEvery { userSessionStore.saveSession(any()) } just runs
        coEvery { featureFlagSyncer.sync() } just runs

        val result = useCase("+919876543210", "123456")

        assertEquals(token, (result as DataResult.Success).data)
    }

    @Test
    fun `error - saveSession not called`() = runTest {
        coEvery { authRepository.verifyOtp(any(), any()) } returns
            DataResult.Error(Exception("fail"), "Invalid OTP")

        useCase("+919876543210", "000000")

        coVerify(exactly = 0) { userSessionStore.saveSession(any()) }
    }

    @Test
    fun `error - featureFlagSyncer not called`() = runTest {
        coEvery { authRepository.verifyOtp(any(), any()) } returns
            DataResult.Error(Exception("fail"), "Invalid OTP")

        useCase("+919876543210", "000000")

        coVerify(exactly = 0) { featureFlagSyncer.sync() }
    }

    @Test
    fun `error - result returned unchanged`() = runTest {
        coEvery { authRepository.verifyOtp(any(), any()) } returns
            DataResult.Error(Exception("fail"), "Invalid OTP")

        val result = useCase("+919876543210", "000000")

        assertTrue(result is DataResult.Error)
        assertEquals("Invalid OTP", (result as DataResult.Error).message)
    }
}
