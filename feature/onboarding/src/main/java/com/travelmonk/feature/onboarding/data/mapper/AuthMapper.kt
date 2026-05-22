package com.travelmonk.feature.onboarding.data.mapper

import com.travelmonk.feature.onboarding.data.api.dto.AuthResponseDto
import com.travelmonk.feature.onboarding.domain.model.AuthToken
import com.travelmonk.feature.onboarding.domain.model.User

fun AuthResponseDto.toToken(): AuthToken = AuthToken(
    accessToken  = accessToken,
    refreshToken = refreshToken,
    userId       = userId,
    phoneNumber  = phone
)

fun AuthResponseDto.toUser(): User = User(
    id = userId,
    phoneNumber = phone
)
