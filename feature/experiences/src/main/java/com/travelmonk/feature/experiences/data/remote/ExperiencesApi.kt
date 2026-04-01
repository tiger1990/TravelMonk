package com.travelmonk.feature.experiences.data.remote

import com.travelmonk.feature.experiences.domain.model.Experience
import retrofit2.http.GET
import retrofit2.http.Query

interface ExperiencesApi {
    @GET("experiences")
    suspend fun getExperiences(
        @Query("category") category: String
    ): List<Experience>
}
