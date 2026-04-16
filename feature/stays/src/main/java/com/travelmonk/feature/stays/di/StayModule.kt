package com.travelmonk.feature.stays.di

import com.travelmonk.feature.stays.data.api.StaysApi
import com.travelmonk.feature.stays.data.repository.StayRepositoryImpl
import com.travelmonk.feature.stays.domain.repository.StayRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StayModule {

    @Binds
    @Singleton
    abstract fun bindStayRepository(
        stayRepositoryImpl: StayRepositoryImpl
    ): StayRepository

    companion object {
        @Provides
        @Singleton
        fun provideStaysApi(retrofit: Retrofit): StaysApi {
            return retrofit.create(StaysApi::class.java)
        }
    }
}
