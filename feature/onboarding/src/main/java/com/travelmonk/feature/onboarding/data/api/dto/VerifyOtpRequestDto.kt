package com.travelmonk.feature.onboarding.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequestDto(
    @SerialName("phone") val phone: String,
    @SerialName("otp")   val otp: String
)
