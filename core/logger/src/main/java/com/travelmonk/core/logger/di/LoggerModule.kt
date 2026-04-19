package com.travelmonk.core.logger.di

import android.content.Context
import com.travelmonk.core.logger.LogFileManager
import com.travelmonk.core.logger.upload.DummyHttpSender
import com.travelmonk.core.logger.upload.RemoteLogSender
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoggerModule {

    @Provides
    @Singleton
    fun provideLogFileManager(@ApplicationContext context: Context): LogFileManager =
        LogFileManager(context)

    @Provides
    @Singleton
    fun provideRemoteLogSender(): RemoteLogSender = DummyHttpSender()
}
