package com.travelmonk.core.logger.di

import android.content.Context
import com.travelmonk.core.logger.LogFileManager
import com.travelmonk.core.logger.TravelMonkLogger
import com.travelmonk.core.logger.upload.DummyHttpSender
import com.travelmonk.core.logger.upload.RemoteLogSender
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for the logger library.
 *
 * Provides [LogFileManager] and [RemoteLogSender] as singletons so that
 * [com.travelmonk.core.logger.viewer.LogViewerViewModel] can be injected
 * with the same instances used by [TravelMonkLogger].
 *
 * Delegates to TravelMonkLogger's already-initialised instances where available,
 * falling back to fresh instances if init() has not been called yet.
 */
@Module
@InstallIn(SingletonComponent::class)
object LoggerModule {

    @Provides
    @Singleton
    fun provideLogFileManager(@ApplicationContext context: Context): LogFileManager =
        TravelMonkLogger.fileManager ?: LogFileManager(context)

    @Provides
    @Singleton
    fun provideRemoteLogSender(): RemoteLogSender =
        TravelMonkLogger.remoteSender ?: DummyHttpSender()
}
