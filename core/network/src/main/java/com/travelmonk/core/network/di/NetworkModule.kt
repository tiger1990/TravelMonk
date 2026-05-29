package com.travelmonk.core.network.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.travelmonk.core.common.config.AppConfig
import com.travelmonk.core.network.security.CertificatePinnerFactory
import com.travelmonk.core.network.security.PinFailureInterceptor
import com.travelmonk.core.network.security.PinSource
import com.travelmonk.core.network.security.StaticPinSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    // Bind StaticPinSource as the active PinSource.
    // To enable remote rotation, swap StaticPinSource for RemotePinSource here.
    @Binds
    @Singleton
    abstract fun bindPinSource(impl: StaticPinSource): PinSource

    companion object {

        @Provides
        @Singleton
        fun provideMoshi(): Moshi {
            return Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
        }

        @Provides
        @Singleton
        fun provideLoggingInterceptor(appConfig: AppConfig): HttpLoggingInterceptor {
            val level = if (appConfig.isDebug) HttpLoggingInterceptor.Level.BODY
                        else HttpLoggingInterceptor.Level.NONE
            return HttpLoggingInterceptor().apply { this.level = level }
        }

        // General-purpose client for non-sensitive APIs (home, flights, stays, experiences, etc.)
        // No certificate pinning — a pin rotation incident on booking/payment does not affect these.
        @Provides
        @Singleton
        fun provideOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor,
            appConfig: AppConfig
        ): OkHttpClient {
            val timeout = appConfig.apiTimeoutSeconds.toLong()
            return OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()
        }

        // Pinned client for booking and payment endpoints.
        // CertificatePinner is a no-op (DEFAULT) in DEV — Charles/mitmproxy works normally.
        // In STAGING and PRODUCTION it enforces SPKI hash pinning and hard-fails on mismatch.
        @Provides
        @Singleton
        @PinnedClient
        fun providePinnedOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor,
            pinnerFactory: CertificatePinnerFactory,
            pinFailureInterceptor: PinFailureInterceptor,
            appConfig: AppConfig
        ): OkHttpClient {
            val timeout = appConfig.apiTimeoutSeconds.toLong()
            return OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .certificatePinner(pinnerFactory.create())
                .addInterceptor(pinFailureInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()
        }

        // Unqualified Retrofit — used by all non-sensitive feature modules (backward-compatible).
        @Provides
        @Singleton
        fun provideRetrofit(
            okHttpClient: OkHttpClient,
            moshi: Moshi,
            appConfig: AppConfig
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(appConfig.baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
        }

        // Pinned Retrofit — must be used by BookingModule and any future payment feature module.
        @Provides
        @Singleton
        @PinnedRetrofit
        fun providePinnedRetrofit(
            @PinnedClient okHttpClient: OkHttpClient,
            moshi: Moshi,
            appConfig: AppConfig
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(appConfig.baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
        }
    }
}
