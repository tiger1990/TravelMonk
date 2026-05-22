package com.travelmonk.feature.onboarding.data.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.onboarding.data.api.AuthApi
import com.travelmonk.feature.onboarding.data.api.dto.SendOtpRequestDto
import com.travelmonk.feature.onboarding.data.api.dto.VerifyOtpRequestDto
import com.travelmonk.feature.onboarding.data.mapper.toToken
import com.travelmonk.feature.onboarding.domain.model.AuthToken
import com.travelmonk.feature.onboarding.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {

    override suspend fun sendOtp(phone: String): DataResult<Unit> = try {
        // TODO: Replace with real API call when backend is integrated:
        // authApi.sendOtp(SendOtpRequestDto(phone))
        DataResult.Success(Unit)
    } catch (e: Exception) {
        DataResult.Error(e, e.message)
    }

    override suspend fun verifyOtp(phone: String, otp: String): DataResult<AuthToken> = try {
        // TODO: Replace with real API call when backend is integrated:
        // DataResult.Success(authApi.verifyOtp(VerifyOtpRequestDto(phone, otp)).toToken())
        DataResult.Success(AuthToken(
            accessToken  = "fake_access_token_abc123",
            refreshToken = "fake_refresh_token_xyz789",
            userId       = "usr_fake_001",
            phoneNumber  = "+919876543210"
        ))
    } catch (e: Exception) {
        DataResult.Error(e, e.message)
    }
}
