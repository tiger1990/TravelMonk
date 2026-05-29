package com.travelmonk.feature.stays.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.model.Stay
import com.travelmonk.feature.stays.domain.repository.StayRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStayDetailsUseCase @Inject constructor(
    private val repository: StayRepository
) {
    operator fun invoke(stayId: String): Flow<DataResult<Stay>> =
        repository.getStayById(stayId)
}
