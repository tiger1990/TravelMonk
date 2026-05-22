package com.travelmonk.feature.onboarding.domain.usecase

import com.travelmonk.core.common.config.FeatureFlagSyncer
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.onboarding.data.local.UserSessionStore
import com.travelmonk.feature.onboarding.domain.model.AuthToken
import com.travelmonk.feature.onboarding.domain.repository.PasskeyRepository
import javax.inject.Inject

class PasskeyAuthUseCase @Inject constructor(
    private val passkeyRepository: PasskeyRepository,
    private val userSessionStore: UserSessionStore,
    private val featureFlagSyncer: FeatureFlagSyncer
) {
    /**
     * @param assertionJson The WebAuthn assertion response JSON from Credential Manager
     */
    suspend operator fun invoke(assertionJson: String): DataResult<AuthToken> {
        val result = passkeyRepository.completeAuthentication(assertionJson)
        if (result is DataResult.Success) {
            userSessionStore.saveSession(result.data)  // userId + phoneNumber carried by AuthToken
            featureFlagSyncer.sync()
        }
        return result
    }
}
