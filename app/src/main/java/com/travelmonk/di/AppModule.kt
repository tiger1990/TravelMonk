package com.travelmonk.di

import android.content.Context
import com.travelmonk.BuildConfig
import com.travelmonk.core.common.config.AppConfig
import com.travelmonk.core.common.config.Environment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppConfig(@ApplicationContext context: Context): AppConfig = object : AppConfig {

        override val isDebug: Boolean = BuildConfig.DEBUG
        override val baseUrl: String  = BuildConfig.BASE_URL
        override val environment: Environment = Environment.valueOf(BuildConfig.ENVIRONMENT)
        override val apiTimeoutSeconds: Int = BuildConfig.API_TIMEOUT_SECONDS
    }
}
