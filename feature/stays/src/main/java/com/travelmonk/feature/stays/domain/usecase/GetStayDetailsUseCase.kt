package com.travelmonk.feature.stays.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.stays.domain.model.Stay
import com.travelmonk.feature.stays.domain.repository.StayRepository
import javax.inject.Inject

class GetStayDetailsUseCase @Inject constructor(
    private val repository: StayRepository
) {
    suspend operator fun invoke(stayId: String): DataResult<Stay> =
        repository.getStayById(stayId)
}
