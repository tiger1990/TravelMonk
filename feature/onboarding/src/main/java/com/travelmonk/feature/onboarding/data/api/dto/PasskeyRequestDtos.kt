package com.travelmonk.feature.onboarding.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasskeyBeginRegistrationRequestDto(
    @SerialName("user_id") val userId: String
)

@Serializable
data class PasskeyCompleteRegistrationRequestDto(
    @SerialName("user_id")          val userId: String,
    @SerialName("attestation_json") val attestationJson: String
)

@Serializable
data class PasskeyCompleteAuthRequestDto(
    @SerialName("assertion_json") val assertionJson: String
)
