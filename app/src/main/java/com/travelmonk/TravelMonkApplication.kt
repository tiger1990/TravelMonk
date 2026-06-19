package com.travelmonk

import android.app.Application
import android.os.StrictMode
import com.travelmonk.core.common.di.IoDispatcher
import com.travelmonk.core.logger.LogFileManager
import com.travelmonk.core.logger.TravelMonkLogger
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class TravelMonkApplication : Application() {

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    lateinit var logFileManager: LogFileManager

    // dagger.Lazy defers OkHttpClient construction until .get() is called.
    // We call it immediately in onCreate on an IO thread so the SSL context
    // and TrustManager are initialised before any Activity touches the network stack.
    // Without this, the first ViewModel that injects a repository triggers OkHttpClient
    // construction on the main thread — a StrictMode CustomViolation (newSSLContext).
    @Inject
    lateinit var okHttpClient: Lazy<OkHttpClient>

    override fun onCreate() {
        super.onCreate()
        // comment below when run in actual device
        if (BuildConfig.DEBUG) {
            setupStrictMode()
        }

        // Pre-warm OkHttpClient on IO so SSL context initialization never runs on the main thread.
        CoroutineScope(ioDispatcher).launch { okHttpClient.get() }

        // Initialize our central logging infrastructure.
        // Because LogFileManager uses lazy I/O and TravelMonkLogger initializes
        // background workers asynchronously, this call is now main-thread safe.
        TravelMonkLogger.init(
            context = this,
            fileManager = logFileManager,
            isDebugBuild = BuildConfig.DEBUG,
            dispatcher = ioDispatcher
        )
    }

    /**
     * Configures StrictMode to detect accidental disk or network operations on the main thread.
     *
     * StrictMode is a developer tool which detects things you might be doing by accident and
     * brings them to your attention so you can fix them. It is most commonly used to catch
     * accidental disk or network access on the application's main thread, where UI operations
     * take place and animations are held.
     *
     * We enable this only in Debug builds to ensure production performance is not affected
     * while catching performance regressions early during development and beta testing.
     */
    private fun setupStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }
}
