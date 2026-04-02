package com.travelmonk.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.travelmonk.BuildConfig
import com.travelmonk.core.common.config.AppConfig
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

        // This is the standard Android way to check for debug status
        // without relying on generated BuildConfig files.
        // override val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
