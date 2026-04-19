package com.travelmonk

import android.app.Application
import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.logger.LogFileManager
import com.travelmonk.core.logger.TravelMonkLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltAndroidApp
class TravelMonkApplication : Application() {

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    lateinit var logFileManager: LogFileManager

    override fun onCreate() {
        super.onCreate()
        
        // Initialize our central logging infrastructure
        TravelMonkLogger.init(
            context = this,
            fileManager = logFileManager,
            isDebugBuild = BuildConfig.DEBUG,
            dispatcher = ioDispatcher
        )
    }
}
