package com.travelmonk.feature.experiences.di

import com.travelmonk.feature.experiences.data.remote.ExperiencesApi
import com.travelmonk.feature.experiences.data.repository.ExperienceRepositoryImpl
import com.travelmonk.feature.experiences.domain.repository.ExperienceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExperienceModule {

    @Binds
    @Singleton
    abstract fun bindExperienceRepository(
        experienceRepositoryImpl: ExperienceRepositoryImpl
    ): ExperienceRepository

    companion object {
        @Provides
        @Singleton
        fun provideExperiencesApi(retrofit: Retrofit): ExperiencesApi {
            return retrofit.create(ExperiencesApi::class.java)
        }
    }
}
