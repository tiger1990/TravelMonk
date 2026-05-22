package com.travelmonk.feature.onboarding.di

import com.travelmonk.feature.onboarding.data.api.AuthApi
import com.travelmonk.feature.onboarding.data.api.PasskeyApi
import com.travelmonk.feature.onboarding.data.repository.AuthRepositoryImpl
import com.travelmonk.feature.onboarding.data.repository.PasskeyRepositoryImpl
import com.travelmonk.feature.onboarding.domain.OtpRateLimiter
import com.travelmonk.feature.onboarding.domain.repository.AuthRepository
import com.travelmonk.feature.onboarding.domain.repository.PasskeyRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPasskeyRepository(impl: PasskeyRepositoryImpl): PasskeyRepository

    companion object {

        @Provides
        @Singleton
        fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

        @Provides
        @Singleton
        fun providePasskeyApi(retrofit: Retrofit): PasskeyApi = retrofit.create(PasskeyApi::class.java)

        // OtpRateLimiter is provided here (not @Inject constructor) because Kotlin emits a
        // synthetic default constructor for each parameter with a default value, which causes
        // Hilt to see two @Inject constructors and fail. Explicit @Provides sidesteps this.
        @Provides
        @Singleton
        fun provideOtpRateLimiter(): OtpRateLimiter = OtpRateLimiter(clock = System::currentTimeMillis)
    }
}
