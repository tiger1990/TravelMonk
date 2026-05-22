package com.travelmonk.feature.onboarding.data.mock

import android.content.Context
import com.travelmonk.feature.onboarding.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Pre-canned passkey ceremony responses for pre-backend integration.
 * Set [isEnabled] = false and remove injection from PasskeyPromptViewModel
 * when the real CredentialManager flow is wired to the backend.
 */
class PasskeyMockDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val isEnabled: Boolean = true

    fun attestationResponseJson(): String =
        context.resources
            .openRawResource(R.raw.mock_passkey_attestation_response)
            .bufferedReader()
            .use { it.readText() }

    fun assertionResponseJson(): String =
        context.resources
            .openRawResource(R.raw.mock_passkey_assertion_response)
            .bufferedReader()
            .use { it.readText() }
}
