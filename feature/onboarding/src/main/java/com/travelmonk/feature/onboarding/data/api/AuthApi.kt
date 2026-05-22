package com.travelmonk.feature.onboarding.data.api

import com.travelmonk.feature.onboarding.data.api.dto.AuthResponseDto
import com.travelmonk.feature.onboarding.data.api.dto.SendOtpRequestDto
import com.travelmonk.feature.onboarding.data.api.dto.VerifyOtpRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/phone/send-otp")
    suspend fun sendOtp(@Body body: SendOtpRequestDto)

    @POST("auth/phone/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequestDto): AuthResponseDto
}
