package com.travelmonk.feature.onboarding.domain.model

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val userId: String = "",
    val phoneNumber: String = ""
)
