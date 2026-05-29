package com.travelmonk.feature.stays.domain.repository

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.model.Stay
import kotlinx.coroutines.flow.Flow

interface StayRepository {
    suspend fun searchStays(location: String): DataResult<List<Stay>>
    // Returns a cold Flow so the detail screen can react to Room updates
    // (migration: replace flow{} with dao.getStayById(id).map { DataResult.Success(it.toDomain()) }).
    fun getStayById(id: String): Flow<DataResult<Stay>>
}
