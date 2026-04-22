package com.travelmonk.feature.stays.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.model.Stay

interface StayRepository {
    suspend fun searchStays(location: String): DataResult<List<Stay>>
    suspend fun getStayById(id: String): DataResult<Stay>
}
