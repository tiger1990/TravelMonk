package com.travelmonk.feature.onboarding.domain.usecase

import com.travelmonk.core.common.config.FeatureFlagSyncer
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.onboarding.data.local.UserSessionStore
import com.travelmonk.feature.onboarding.domain.model.AuthToken
import com.travelmonk.feature.onboarding.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userSessionStore: UserSessionStore,
    private val featureFlagSyncer: FeatureFlagSyncer
) {
    suspend operator fun invoke(phone: String, otp: String): DataResult<AuthToken> {
        val result = authRepository.verifyOtp(phone, otp)
        if (result is DataResult.Success) {
            userSessionStore.saveSession(result.data)  // userId + phoneNumber carried by AuthToken
            featureFlagSyncer.sync()
        }
        return result
    }
}
