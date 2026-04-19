package com.travelmonk.core.logger.upload

import com.travelmonk.core.logger.LogFileManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class LogUploadOrchestratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private fun makeFile(name: String = "log_test.txt"): File =
        tempFolder.newFile(name).also { it.writeText("{\"msg\":\"test\"}\n") }

    @Test
    fun `uploadAllPending deletes file on Success`() = testScope.runTest {
        val file = makeFile()
        val fileManager = mockk<LogFileManager>()
        val sender = mockk<RemoteLogSender>()

        every { fileManager.rotationEvents } returns MutableSharedFlow()
        every { fileManager.getPendingFiles() } returns listOf(file)
        coEvery { sender.sendBatch(file) } returns UploadResult.Success
        every { fileManager.deleteAfterUpload(file) } answers { file.delete(); Unit }

        val orchestrator = LogUploadOrchestrator(fileManager, sender, this)
        orchestrator.uploadAllPending()

        verify(exactly = 1) { fileManager.deleteAfterUpload(file) }
    }

    @Test
    fun `uploadAllPending leaves file on Failure`() = testScope.runTest {
        val file = makeFile()
        val fileManager = mockk<LogFileManager>()
        val sender = mockk<RemoteLogSender>()

        every { fileManager.rotationEvents } returns MutableSharedFlow()
        every { fileManager.getPendingFiles() } returns listOf(file)
        coEvery { sender.sendBatch(file) } returns UploadResult.Failure(
            retryable = true,
            cause = RuntimeException("network error")
        )

        val orchestrator = LogUploadOrchestrator(fileManager, sender, this)
        orchestrator.uploadAllPending()

        verify(exactly = 0) { fileManager.deleteAfterUpload(any()) }
    }

    @Test
    fun `rotation event triggers upload and deletes file on Success`() = testScope.runTest {
        val file = makeFile()
        val rotationFlow = MutableSharedFlow<File>(extraBufferCapacity = 1)

        val fileManager = mockk<LogFileManager>()
        val sender = mockk<RemoteLogSender>()

        every { fileManager.rotationEvents } returns rotationFlow
        coEvery { sender.sendBatch(file) } returns UploadResult.Success
        every { fileManager.deleteAfterUpload(file) } answers { file.delete(); Unit }

        // backgroundScope is cancelled automatically at test end — safe for infinite collectors
        val orchestrator = LogUploadOrchestrator(fileManager, sender, backgroundScope)
        orchestrator.start()

        rotationFlow.emit(file)

        coVerify(exactly = 1) { sender.sendBatch(file) }
        verify(exactly = 1) { fileManager.deleteAfterUpload(file) }
    }

    @Test
    fun `rotation event leaves file when upload fails`() = testScope.runTest {
        val file = makeFile()
        val rotationFlow = MutableSharedFlow<File>(extraBufferCapacity = 1)

        val fileManager = mockk<LogFileManager>()
        val sender = mockk<RemoteLogSender>()

        every { fileManager.rotationEvents } returns rotationFlow
        coEvery { sender.sendBatch(file) } returns UploadResult.Failure(
            retryable = true,
            cause = RuntimeException("timeout")
        )

        val orchestrator = LogUploadOrchestrator(fileManager, sender, backgroundScope)
        orchestrator.start()

        rotationFlow.emit(file)

        coVerify(exactly = 1) { sender.sendBatch(file) }
        verify(exactly = 0) { fileManager.deleteAfterUpload(any()) }
    }

    @Test
    fun `uploadAllPending handles empty pending list without error`() = testScope.runTest {
        val fileManager = mockk<LogFileManager>()
        val sender = mockk<RemoteLogSender>()

        every { fileManager.rotationEvents } returns MutableSharedFlow()
        every { fileManager.getPendingFiles() } returns emptyList()

        val orchestrator = LogUploadOrchestrator(fileManager, sender, this)
        orchestrator.uploadAllPending()

        coVerify(exactly = 0) { sender.sendBatch(any()) }
    }

    @Test
    fun `uploadAllPending uploads all pending files`() = testScope.runTest {
        val file1 = makeFile("log_1.txt")
        val file2 = makeFile("log_2.txt")

        val fileManager = mockk<LogFileManager>()
        val sender = mockk<RemoteLogSender>()

        every { fileManager.rotationEvents } returns MutableSharedFlow()
        every { fileManager.getPendingFiles() } returns listOf(file1, file2)
        coEvery { sender.sendBatch(any()) } returns UploadResult.Success
        every { fileManager.deleteAfterUpload(any()) } returns Unit

        val orchestrator = LogUploadOrchestrator(fileManager, sender, this)
        orchestrator.uploadAllPending()

        coVerify(exactly = 1) { sender.sendBatch(file1) }
        coVerify(exactly = 1) { sender.sendBatch(file2) }
        verify(exactly = 2) { fileManager.deleteAfterUpload(any()) }
    }
}
