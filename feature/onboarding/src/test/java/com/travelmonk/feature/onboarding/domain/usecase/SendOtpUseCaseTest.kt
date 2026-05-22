package com.travelmonk.feature.onboarding.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.core.testing.MainDispatcherRule
import com.travelmonk.feature.onboarding.domain.OtpRateLimiter
import com.travelmonk.feature.onboarding.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val PHONE = "+919876543210"

class SendOtpUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val rateLimiter: OtpRateLimiter = mockk()
    private lateinit var useCase: SendOtpUseCase

    @Before
    fun setup() {
        useCase = SendOtpUseCase(authRepository, rateLimiter)
    }

    // ── Allowed ───────────────────────────────────────────────────────────────

    @Test
    fun `allowed - delegates to repository and returns success`() = runTest {
        every { rateLimiter.checkAndRecord(PHONE) } returns OtpRateLimiter.Result.Allowed
        coEvery { authRepository.sendOtp(PHONE) } returns DataResult.Success(Unit)

        val result = useCase(PHONE)

        assertEquals(DataResult.Success(Unit), result)
        coVerify(exactly = 1) { authRepository.sendOtp(PHONE) }
    }

    @Test
    fun `allowed - repository error is returned unchanged`() = runTest {
        every { rateLimiter.checkAndRecord(PHONE) } returns OtpRateLimiter.Result.Allowed
        coEvery { authRepository.sendOtp(PHONE) } returns
            DataResult.Error(Exception("Server error"), "Server error")

        val result = useCase(PHONE)

        assertTrue(result is DataResult.Error)
        assertEquals("Server error", (result as DataResult.Error).message)
    }

    // ── Throttled ─────────────────────────────────────────────────────────────

    @Test
    fun `throttled - returns error without calling repository`() = runTest {
        every { rateLimiter.checkAndRecord(PHONE) } returns
            OtpRateLimiter.Result.Throttled(retryAfterMs = 120_000L)

        val result = useCase(PHONE)

        assertTrue(result is DataResult.Error)
        coVerify(exactly = 0) { authRepository.sendOtp(any()) }
    }

    @Test
    fun `throttled - error message includes wait time in minutes`() = runTest {
        every { rateLimiter.checkAndRecord(PHONE) } returns
            OtpRateLimiter.Result.Throttled(retryAfterMs = 120_000L) // 2 minutes

        val result = useCase(PHONE) as DataResult.Error

        assertTrue(result.message?.contains("2m") == true)
    }

    @Test
    fun `throttled - sub-minute wait rounds up to 1 minute`() = runTest {
        every { rateLimiter.checkAndRecord(PHONE) } returns
            OtpRateLimiter.Result.Throttled(retryAfterMs = 30_000L) // 30 seconds

        val result = useCase(PHONE) as DataResult.Error

        assertTrue(result.message?.contains("1m") == true)
    }
}