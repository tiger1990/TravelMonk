package com.travelmonk.feature.services.di

import com.travelmonk.feature.services.data.api.ServicesApi
import com.travelmonk.feature.services.data.repository.ServiceRepositoryImpl
import com.travelmonk.feature.services.domain.repository.ServiceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindServiceRepository(
        serviceRepositoryImpl: ServiceRepositoryImpl
    ): ServiceRepository

    companion object {
        @Provides
        @Singleton
        fun provideServicesApi(retrofit: Retrofit): ServicesApi {
            return retrofit.create(ServicesApi::class.java)
        }
    }
}
