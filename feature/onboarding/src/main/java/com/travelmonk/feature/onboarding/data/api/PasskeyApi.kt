package com.travelmonk.feature.onboarding.data.api

import com.travelmonk.feature.onboarding.data.api.dto.AuthResponseDto
import com.travelmonk.feature.onboarding.data.api.dto.PasskeyBeginRegistrationRequestDto
import com.travelmonk.feature.onboarding.data.api.dto.PasskeyChallengeDto
import com.travelmonk.feature.onboarding.data.api.dto.PasskeyCompleteAuthRequestDto
import com.travelmonk.feature.onboarding.data.api.dto.PasskeyCompleteRegistrationRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PasskeyApi {
    @POST("auth/passkey/register/begin")
    suspend fun beginRegistration(@Body body: PasskeyBeginRegistrationRequestDto): PasskeyChallengeDto

    @POST("auth/passkey/register/complete")
    suspend fun completeRegistration(@Body body: PasskeyCompleteRegistrationRequestDto): AuthResponseDto

    @POST("auth/passkey/auth/begin")
    suspend fun beginAuthentication(): PasskeyChallengeDto

    @POST("auth/passkey/auth/complete")
    suspend fun completeAuthentication(@Body body: PasskeyCompleteAuthRequestDto): AuthResponseDto
}
