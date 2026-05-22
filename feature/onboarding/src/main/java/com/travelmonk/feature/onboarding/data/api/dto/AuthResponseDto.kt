package com.travelmonk.feature.onboarding.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    @SerialName("access_token")  val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user_id")       val userId: String,
    @SerialName("phone")         val phone: String
)
