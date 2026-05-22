package com.travelmonk.feature.onboarding.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.onboarding.domain.OtpRateLimiter
import com.travelmonk.feature.onboarding.domain.repository.AuthRepository
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val rateLimiter: OtpRateLimiter
) {
    suspend operator fun invoke(phone: String): DataResult<Unit> =
        when (val limit = rateLimiter.checkAndRecord(phone)) {
            is OtpRateLimiter.Result.Allowed -> authRepository.sendOtp(phone)
            is OtpRateLimiter.Result.Throttled -> {
                val waitMin = (limit.retryAfterMs / 60_000).coerceAtLeast(1)
                DataResult.Error(
                    exception = IllegalStateException("OTP rate limited"),
                    message = "Too many attempts. Try again in ${waitMin}m."
                )
            }
        }
}
