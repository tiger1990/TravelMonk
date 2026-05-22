package com.travelmonk.feature.home.domain.usecase

import com.travelmonk.core.common.result.DataResult
import com.travelmonk.feature.home.domain.model.HomeBanner
import com.travelmonk.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHomeBannersUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    operator fun invoke(): Flow<DataResult<List<HomeBanner>>> = repository.getHomeBanners()
}
