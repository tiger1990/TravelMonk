package com.travelmonk.feature.onboarding.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequestDto(
    @SerialName("phone") val phone: String
)
