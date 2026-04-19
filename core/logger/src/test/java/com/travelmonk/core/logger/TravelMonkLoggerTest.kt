package com.travelmonk.core.logger

import androidx.work.Configuration
import androidx.work.WorkManager
import com.travelmonk.core.logger.upload.DummyHttpSender
import com.travelmonk.core.logger.upload.RemoteLogSender
import com.travelmonk.core.logger.upload.UploadResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

/**
 * Integration tests for TravelMonkLogger.
 *
 * Async tests inject `backgroundScope` into init() so all coroutines run on
 * StandardTestDispatcher (the virtual-clock scheduler). This lets us use
 * runCurrent() to drive execution deterministically instead of real wall-clock
 * delays.
 *
 * Why runCurrent() and not advanceUntilIdle()?
 * LogProcessor uses withTimeoutOrNull(5_000L) in an infinite loop. advanceUntilIdle()
 * would repeatedly advance 5s of virtual time per iteration, looping forever.
 * runCurrent() only dispatches coroutines already queued — it stops when the queue
 * is empty, which happens as soon as the pending receive() suspends again.
 *
 * Why backgroundScope and not `this`?
 * LogUploadOrchestrator collects from rotationEvents indefinitely. Passing `this`
 * (the TestScope) would cause UncompletedCoroutinesError at test end. backgroundScope
 * auto-cancels infinite coroutines silently when the test body finishes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TravelMonkLoggerTest {

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        try {
            WorkManager.initialize(context, Configuration.Builder().build())
        } catch (_: IllegalStateException) {
            // Already initialized in a previous test — safe to ignore
        }
        // Cancels all running coroutines, drains channels, and resets all fields.
        // Without this, multiple CriticalEventUploader instances from prior init() calls
        // would all compete to receive from criticalChannel — making sendCritical tests flaky.
        TravelMonkLogger.resetForTest()
    }

    @After
    fun tearDown() {
        File(RuntimeEnvironment.getApplication().filesDir, "logs").deleteRecursively()
    }

    // ── State / init tests (synchronous — no scope injection needed) ──────────

    @Test
    fun `log calls before init are silently dropped`() {
        TravelMonkLogger.d("Tag", "before init")
        TravelMonkLogger.e("Tag", "error before init")
        // No exception = pass
    }

    @Test
    fun `fileManager is null before init`() {
        assertNull(TravelMonkLogger.fileManager)
    }

    @Test
    fun `init sets fileManager and remoteSender`() {
        TravelMonkLogger.init(RuntimeEnvironment.getApplication(), isDebugBuild = false)

        assertNotNull(TravelMonkLogger.fileManager)
        assertNotNull(TravelMonkLogger.remoteSender)
    }

    @Test
    fun `init is idempotent — second call is a no-op`() {
        val context = RuntimeEnvironment.getApplication()
        TravelMonkLogger.init(context, isDebugBuild = false)
        val firstFileManager = TravelMonkLogger.fileManager

        TravelMonkLogger.init(context, isDebugBuild = false)

        assert(TravelMonkLogger.fileManager === firstFileManager) {
            "Second init() must not replace the fileManager instance"
        }
    }

    @Test
    fun `init uses DummyHttpSender when no remote sender provided`() {
        TravelMonkLogger.init(RuntimeEnvironment.getApplication(), isDebugBuild = false)

        assert(TravelMonkLogger.remoteSender is DummyHttpSender) {
            "Default sender should be DummyHttpSender"
        }
    }

    @Test
    fun `init uses provided custom remote sender`() {
        val customSender = mockk<RemoteLogSender>(relaxed = true)
        TravelMonkLogger.init(
            RuntimeEnvironment.getApplication(),
            isDebugBuild = false,
            remote = customSender
        )

        assert(TravelMonkLogger.remoteSender === customSender) {
            "Custom sender should be stored and used"
        }
    }

    @Test
    fun `all log level helpers route through the pipeline without error`() {
        TravelMonkLogger.init(RuntimeEnvironment.getApplication(), isDebugBuild = false)

        TravelMonkLogger.v("Tag", "verbose", mapOf("k" to "v"))
        TravelMonkLogger.d("Tag", "debug", mapOf("k" to 1))
        TravelMonkLogger.i("Tag", "info")
        TravelMonkLogger.w("Tag", "warn", RuntimeException("test"))
        TravelMonkLogger.e("Tag", "error", RuntimeException("test"), mapOf("code" to 500))
        // No exception = pass
    }

    @Test
    fun `init with isDebugBuild=true sets TraceContext debugMode to true`() {
        TravelMonkLogger.init(RuntimeEnvironment.getApplication(), isDebugBuild = true)
        assert(TraceContext.debugMode) {
            "TraceContext.debugMode should be true after init(isDebugBuild = true)"
        }
    }

    // ── Async pipeline tests (scope-injected, deterministic) ─────────────────

    @Test
    fun `ERROR log triggers sendCritical on remote sender`() = runTest {
        val sender = mockk<RemoteLogSender>()
        coEvery { sender.sendBatch(any()) } returns UploadResult.Success
        coEvery { sender.sendCritical(any()) } returns UploadResult.Success

        TravelMonkLogger.init(
            RuntimeEnvironment.getApplication(),
            isDebugBuild = false,
            remote = sender,
            scope = backgroundScope   // StandardTestDispatcher — queues, not auto-runs
        )
        TravelMonkLogger.e("AuthRepo", "Token refresh failed")

        // Flush the dispatcher queue: CriticalEventUploader receives the event
        // and calls sendCritical(). Stops before advancing the 5s LogProcessor timeout.
        runCurrent()

        coVerify(atLeast = 1) { sender.sendCritical(match { it.level == LogLevel.ERROR }) }
    }

    @Test
    fun `non-ERROR logs do not trigger sendCritical`() = runTest {
        val sender = mockk<RemoteLogSender>(relaxed = true)
        coEvery { sender.sendBatch(any()) } returns UploadResult.Success
        coEvery { sender.sendCritical(any()) } returns UploadResult.Success

        TravelMonkLogger.init(
            RuntimeEnvironment.getApplication(),
            isDebugBuild = false,
            remote = sender,
            scope = backgroundScope
        )
        TravelMonkLogger.v("Tag", "verbose")
        TravelMonkLogger.d("Tag", "debug")
        TravelMonkLogger.i("Tag", "info")
        TravelMonkLogger.w("Tag", "warn")

        runCurrent()

        coVerify(exactly = 0) { sender.sendCritical(any()) }
    }

    @Test
    fun `log with TraceContext captures traceId in file`() = runTest {
        TravelMonkLogger.init(
            RuntimeEnvironment.getApplication(),
            isDebugBuild = false,
            scope = backgroundScope
        )

        val trace = TraceContext.new()
        withContext(trace) {
            TravelMonkLogger.i("TraceTag", "traced message")
        }

        runCurrent()

        val activeFile = TravelMonkLogger.fileManager!!.getActiveFile()
        if (activeFile.exists() && activeFile.length() > 0) {
            val content = activeFile.readText()
            assert(content.contains(trace.traceId)) {
                "Expected traceId ${trace.traceId} in file. Content:\n$content"
            }
        }
    }

    @Test
    fun `log within TraceContext with flow writes flow field to log file`() = runTest {
        TravelMonkLogger.init(
            RuntimeEnvironment.getApplication(),
            isDebugBuild = false,
            scope = backgroundScope
        )

        val trace = TraceContext.new(flow = "CheckoutFlow")
        withContext(trace) {
            TravelMonkLogger.i("PaymentVM", "Order placed")
        }

        runCurrent()

        val activeFile = TravelMonkLogger.fileManager!!.getActiveFile()
        if (activeFile.exists() && activeFile.length() > 0) {
            val content = activeFile.readText()
            assert(content.contains("CheckoutFlow")) {
                "Expected 'CheckoutFlow' in log file. Content:\n$content"
            }
        }
    }
}
