package com.travelmonk.feature.onboarding.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.onboarding.domain.model.AuthToken

interface PasskeyRepository {
    /** Begin passkey registration ceremony — returns server challenge JSON. */
    suspend fun beginRegistration(userId: String): DataResult<String>

    /** Complete passkey registration — sends attestation JSON, returns session token. */
    suspend fun completeRegistration(userId: String, attestationJson: String): DataResult<AuthToken>

    /** Begin passkey authentication ceremony — returns server challenge JSON. */
    suspend fun beginAuthentication(): DataResult<String>

    /** Complete passkey authentication — sends assertion JSON, returns session token. */
    suspend fun completeAuthentication(assertionJson: String): DataResult<AuthToken>
}
