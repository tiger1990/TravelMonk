package com.travelmonk.feature.onboarding.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Server-issued WebAuthn challenge for both registration and authentication ceremonies. */
@Serializable
data class PasskeyChallengeDto(
    @SerialName("challenge_json") val challengeJson: String
)
