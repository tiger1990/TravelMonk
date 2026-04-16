package com.travelmonk.feature.stays.domain.usecase

import com.travelmonk.feature.stays.domain.model.Stay
import com.travelmonk.feature.stays.domain.repository.StayRepository
import javax.inject.Inject

class SearchStaysUseCase @Inject constructor(
    private val repository: StayRepository
) {
    suspend operator fun invoke(location: String): List<Stay> =
        repository.searchStays(location)
}
