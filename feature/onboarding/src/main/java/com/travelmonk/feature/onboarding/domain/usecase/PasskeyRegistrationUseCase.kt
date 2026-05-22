package com.travelmonk.feature.onboarding.domain.usecase

import com.travelmonk.core.common.config.FeatureFlagSyncer
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.onboarding.data.local.UserSessionStore
import com.travelmonk.feature.onboarding.domain.model.AuthToken
import com.travelmonk.feature.onboarding.domain.repository.PasskeyRepository
import javax.inject.Inject

class PasskeyRegistrationUseCase @Inject constructor(
    private val passkeyRepository: PasskeyRepository,
    private val userSessionStore: UserSessionStore,
    private val featureFlagSyncer: FeatureFlagSyncer
) {
    /**
     * @param userId The authenticated user's ID (from the OTP session)
     * @param attestationJson The WebAuthn attestation response JSON from Credential Manager
     */
    suspend operator fun invoke(userId: String, attestationJson: String): DataResult<AuthToken> {
        val result = passkeyRepository.completeRegistration(userId, attestationJson)
        if (result is DataResult.Success) {
            userSessionStore.saveSession(result.data)  // userId + phoneNumber carried by AuthToken
            userSessionStore.markPasskeyRegistered()
            featureFlagSyncer.sync()
        }
        return result
    }
}
