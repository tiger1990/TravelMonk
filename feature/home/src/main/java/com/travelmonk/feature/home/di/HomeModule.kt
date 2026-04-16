package com.travelmonk.feature.home.di

import com.travelmonk.feature.home.data.api.HomeApi
import com.travelmonk.feature.home.data.repository.HomeRepositoryImpl
import com.travelmonk.feature.home.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    /**
     * Binds [HomeRepositoryImpl] as the [HomeRepository] implementation.
     * Resolved by Hilt/KSP at compile time — no direct call site exists in source.
     */
    @Suppress("unused")
    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository

    companion object {
        @Provides
        @Singleton
        fun provideHomeApi(retrofit: Retrofit): HomeApi {
            return retrofit.create(HomeApi::class.java)
        }
    }
}
