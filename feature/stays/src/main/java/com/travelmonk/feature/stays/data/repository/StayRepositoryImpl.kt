package com.travelmonk.feature.stays.data.repository

import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.data.api.StaysApi
import com.travelmonk.feature.stays.domain.model.Stay
import com.travelmonk.feature.stays.domain.repository.StayRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StayRepositoryImpl @Inject constructor(
    private val staysApi: StaysApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StayRepository {
    override suspend fun searchStays(location: String): DataResult<List<Stay>> =
        withContext(ioDispatcher) {
            try {
                DataResult.Success(staysApi.searchStays(location))
            } catch (e: Exception) {
                DataResult.Error(e)
                /**
                 * listOf(
                    Stay("1", "The Grand Oberoi", location, "$240", "4.9", "https://images.unsplash.com/photo-1566073771259-6a8506099945"),
                    Stay("2", "Azure Apartment", location, "$180", "4.7", "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267")
                       )
                 */
            }
        }
}
