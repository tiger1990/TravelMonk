package com.travelmonk.feature.home.domain.usecase

import com.travelmonk.feature.home.domain.model.HomeBanner
import com.travelmonk.feature.home.domain.repository.HomeRepository
import javax.inject.Inject

class GetHomeBannersUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(): List<HomeBanner> =
        repository.getHomeBanners()
}
