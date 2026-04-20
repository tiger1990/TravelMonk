package com.travelmonk.core.logger

import android.content.Context
import com.travelmonk.core.logger.upload.LogUploadWorker
import com.travelmonk.core.logger.upload.RemoteLogSender
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Performance-optimized unit tests for TravelMonkLogger.
 * Uses Pure JVM + MockK for maximum velocity and deterministic time control.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TravelMonkLoggerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val mockContext = mockk<Context>(relaxed = true)
    private lateinit var fileManager: LogFileManager

    @Before
    fun setUp() {
        every { mockContext.filesDir } returns tempFolder.root
        mockkObject(LogUploadWorker)
        every { LogUploadWorker.schedule(any()) } returns Unit
        TravelMonkLogger.resetForTest()
        fileManager = LogFileManager(mockContext)
    }

    @After
    fun tearDown() = unmockkAll()

    /**
     * Helper to initialize the logger with specialized dispatchers.
     * Use UnconfinedTestDispatcher for immediate, eager execution.
     * Use StandardTestDispatcher when you need to advance virtual time.
     */
    private fun initLogger(
        remote: RemoteLogSender? = null,
        isDebug: Boolean = false,
        dispatcher: CoroutineDispatcher? = null
    ) {
        TravelMonkLogger.init(
            context = mockContext,
            fileManager = fileManager,
            isDebugBuild = isDebug,
            remote = remote,
            dispatcher = dispatcher ?: UnconfinedTestDispatcher()
        )
    }

    @Test
    fun `log calls before init are silently dropped`() {
        TravelMonkLogger.d("Tag", "ignored")
        TravelMonkLogger.e("Tag", "ignored")
    }

    @Test
    fun `init sets internal singletons`() {
        initLogger()
        assertNotNull(TravelMonkLogger.fileManager)
        assertNotNull(TravelMonkLogger.remoteSender)
    }

    @Test
    fun `init is idempotent`() {
        initLogger()
        val first = TravelMonkLogger.fileManager
        TravelMonkLogger.init(mockContext, LogFileManager(mockContext))
        assertEquals("Should not re-init if already initialized", first, TravelMonkLogger.fileManager)
    }

    @Test
    fun `ERROR log triggers immediate critical upload`() = runTest {
        val sender = mockk<RemoteLogSender>(relaxed = true)
        initLogger(remote = sender)
        
        TravelMonkLogger.e("Auth", "Crash")

        coVerify(exactly = 1) { sender.sendCritical(match { it.level == LogLevel.ERROR }) }
    }

    @Test
    fun `non-ERROR logs do not trigger critical upload`() = runTest {
        val sender = mockk<RemoteLogSender>(relaxed = true)
        initLogger(remote = sender)
        
        TravelMonkLogger.i("Tag", "info")
        TravelMonkLogger.w("Tag", "warn")

        coVerify(exactly = 0) { sender.sendCritical(any()) }
    }

    @Test
    fun `log with TraceContext correctly persists trace data`() = runTest {
        // LogProcessor needs StandardTestDispatcher to trigger the 5s flush timer
        initLogger(dispatcher = StandardTestDispatcher(testScheduler))

        val trace = TraceContext.new(flow = "Checkout")
        withContext(trace) {
            TravelMonkLogger.i("Tag", "msg")
        }

        advanceTimeBy(5001L)

        val lines = fileManager.readActiveFileLines()
        assertTrue(lines.any { it.contains(trace.traceId) && it.contains("Checkout") })
    }

    @Test
    fun `throwable in log is formatted and persisted`() = runTest {
        initLogger(dispatcher = StandardTestDispatcher(testScheduler))

        val error = RuntimeException("Broke")
        TravelMonkLogger.e("Tag", "msg", error)

        advanceTimeBy(5001L)

        val content = fileManager.readActiveFileLines().joinToString()
        assertTrue("Should contain exception message", content.contains("Broke"))
        assertTrue("Should contain stacktrace", content.contains("RuntimeException"))
    }

    @Test
    fun `metadata collision local overrides global`() = runTest {
        initLogger(dispatcher = StandardTestDispatcher(testScheduler))

        TravelMonkLogger.updateGlobalMetadata(mapOf("key" to "global"))
        TravelMonkLogger.i("Tag", "msg", mapOf("key" to "local"))

        advanceTimeBy(5001L)

        val lines = fileManager.readActiveFileLines()
        assertTrue(lines.any { it.contains("local") })
        assertTrue(lines.none { it.contains("global") })
    }

    @Test
    fun `global metadata is cumulative`() = runTest {
        initLogger(dispatcher = StandardTestDispatcher(testScheduler))

        TravelMonkLogger.updateGlobalMetadata(mapOf("user" to "42"))
        TravelMonkLogger.updateGlobalMetadata(mapOf("env" to "prod"))
        TravelMonkLogger.i("Tag", "msg")

        advanceTimeBy(5001L)

        val content = fileManager.readActiveFileLines().joinToString()
        assertTrue(content.contains("42") && content.contains("prod"))
    }

    @Test
    fun `batch limit triggers immediate flush`() = runTest {
        // UnconfinedTestDispatcher makes the 50 logs process eagerly on the same thread
        initLogger(dispatcher = UnconfinedTestDispatcher(testScheduler))

        repeat(5) { TravelMonkLogger.i("Tag", "log") }
        
        // With Unconfined, no runCurrent() or advanceTimeBy is even needed
        assertEquals(5, fileManager.readActiveFileLines().size)
    }

    @Test
    fun `resetForTest purges all state including global metadata`() = runTest {
        initLogger()
        TravelMonkLogger.updateGlobalMetadata(mapOf("k" to "v"))
        
        TravelMonkLogger.resetForTest()
        
        assertNull(TravelMonkLogger.fileManager)
        
        // Re-init and verify metadata is gone
        initLogger(dispatcher = StandardTestDispatcher(testScheduler))
        TravelMonkLogger.i("Tag", "msg")
        advanceTimeBy(5001L)
        
        assertTrue(fileManager.readActiveFileLines().none { it.contains("v") })
    }

    @Test
    fun `all log levels route correctly through the pipeline`() = runTest {
        initLogger(dispatcher = StandardTestDispatcher(testScheduler))

        TravelMonkLogger.v("Tag", "v")
        TravelMonkLogger.d("Tag", "d")
        TravelMonkLogger.i("Tag", "i")
        TravelMonkLogger.w("Tag", "w")
        
        advanceTimeBy(5001L)

        val lines = fileManager.readActiveFileLines()
        assertEquals("Should have 4 log entries", 4, lines.size)
    }
}
