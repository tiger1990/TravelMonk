package com.travelmonk.feature.onboarding.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.onboarding.domain.model.AuthToken

interface AuthRepository {
    suspend fun sendOtp(phone: String): DataResult<Unit>
    suspend fun verifyOtp(phone: String, otp: String): DataResult<AuthToken>
}
