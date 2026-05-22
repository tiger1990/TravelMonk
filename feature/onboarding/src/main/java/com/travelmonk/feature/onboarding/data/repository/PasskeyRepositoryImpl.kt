package com.travelmonk.feature.onboarding.data.repository

import android.content.Context
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.data.api.PasskeyApi
import com.travelmonk.feature.onboarding.domain.model.AuthToken
import com.travelmonk.feature.onboarding.domain.repository.PasskeyRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PasskeyRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @Suppress("unused") private val passkeyApi: PasskeyApi // used when backend is integrated (see TODO comments)
) : PasskeyRepository {

    override suspend fun beginRegistration(userId: String): DataResult<String> = try {
        // TODO: Replace with real API call when backend is integrated:
        // DataResult.Success(passkeyApi.beginRegistration(PasskeyBeginRegistrationRequestDto(userId)).challengeJson)
        val challengeJson = context.resources
            .openRawResource(R.raw.mock_passkey_registration_challenge)
            .bufferedReader()
            .use { it.readText() }
        DataResult.Success(challengeJson)
    } catch (e: Exception) {
        DataResult.Error(e, e.message)
    }

    override suspend fun completeRegistration(userId: String, attestationJson: String): DataResult<AuthToken> = try {
        // TODO: Replace with real API call when backend is integrated:
        // DataResult.Success(passkeyApi.completeRegistration(PasskeyCompleteRegistrationRequestDto(userId, attestationJson)).toToken())
        DataResult.Success(AuthToken(
            accessToken  = "fake_passkey_access_token",
            refreshToken = "fake_passkey_refresh_token",
            userId       = "usr_fake_001",
            phoneNumber  = "+919876543210"
        ))
    } catch (e: Exception) {
        DataResult.Error(e, e.message)
    }

    override suspend fun beginAuthentication(): DataResult<String> = try {
        // TODO: Replace with real API call when backend is integrated:
        // DataResult.Success(passkeyApi.beginAuthentication().challengeJson)
        val challengeJson = context.resources
            .openRawResource(R.raw.mock_passkey_auth_challenge)
            .bufferedReader()
            .use { it.readText() }
        DataResult.Success(challengeJson)
    } catch (e: Exception) {
        DataResult.Error(e, e.message)
    }

    override suspend fun completeAuthentication(assertionJson: String): DataResult<AuthToken> = try {
        // TODO: Replace with real API call when backend is integrated:
        // DataResult.Success(passkeyApi.completeAuthentication(PasskeyCompleteAuthRequestDto(assertionJson)).toToken())
        DataResult.Success(AuthToken(
            accessToken  = "fake_passkey_access_token",
            refreshToken = "fake_passkey_refresh_token",
            userId       = "usr_fake_001",
            phoneNumber  = "+919876543210"
        ))
    } catch (e: Exception) {
        DataResult.Error(e, e.message)
    }
}
