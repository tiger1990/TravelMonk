package com.travelmonk.feature.experiences.data.api

import com.travelmonk.feature.experiences.data.api.dto.ExperienceDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ExperiencesApi {
    @GET("experiences")
    suspend fun getExperiences(
        @Query("category") category: String
    ): List<ExperienceDto>
}
