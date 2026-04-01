package com.travelmonk.feature.stays.domain.repository

import com.travelmonk.feature.stays.domain.model.Stay

interface StayRepository {
    suspend fun searchStays(location: String): List<Stay>
}
