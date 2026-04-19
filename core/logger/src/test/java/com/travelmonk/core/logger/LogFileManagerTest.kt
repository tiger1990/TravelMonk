package com.travelmonk.core.logger

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * High-performance unit tests for LogFileManager.
 * Avoids Robolectric by mocking Context and using TemporaryFolder.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LogFileManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val mockContext = mockk<Context>(relaxed = true)
    private lateinit var fileManager: LogFileManager

    @Before
    fun setup() {
        every { mockContext.filesDir } returns tempFolder.root
        fileManager = LogFileManager(mockContext)
    }

    private fun makeEvent(message: String = "test event") = LogEvent(
        timestamp = System.currentTimeMillis(),
        level = LogLevel.INFO,
        tag = "Test",
        message = message
    )

    @Test
    fun `writeBatch writes lines to active file`() = runTest {
        fileManager.writeBatch(listOf(makeEvent("hello"), makeEvent("world")))

        val lines = fileManager.readActiveFileLines()
        assertTrue("Expected 'hello' in active file", lines.any { it.contains("hello") })
        assertTrue("Expected 'world' in active file", lines.any { it.contains("world") })
    }

    @Test
    fun `writeBatch rotates file when size exceeds 512KB`() = runTest {
        // Each serialized LogEvent with a 1KB message is ~1.1KB → 530 events ≈ 580KB > 512KB cap
        val bigMsg = "x".repeat(1024)
        fileManager.writeBatch(List(530) { makeEvent(bigMsg) })

        assertTrue(
            "File should rotate into pending/",
            fileManager.getPendingFiles().isNotEmpty()
        )
    }

    @Test
    fun `getPendingFiles is empty before any rotation`() {
        assertTrue(fileManager.getPendingFiles().isEmpty())
    }

    @Test
    fun `deleteAfterUpload removes file from pending`() = runTest {
        val bigMsg = "x".repeat(1024)
        fileManager.writeBatch(List(530) { makeEvent(bigMsg) })

        val pending = fileManager.getPendingFiles()
        assertTrue(pending.isNotEmpty())

        val file = pending.first()
        fileManager.deleteAfterUpload(file)
        assertFalse("Deleted file should no longer exist", file.exists())
    }

    @Test
    fun `app restart resumes writing to same file when under 512KB`() = runTest {
        fileManager.writeBatch(listOf(makeEvent("session1")))
        val linesBefore = fileManager.readActiveFileLines().size

        // Simulate app restart — new LogFileManager instance, same context
        val fileManager2 = LogFileManager(mockContext)
        fileManager2.writeBatch(listOf(makeEvent("session2")))

        val linesAfter = fileManager2.readActiveFileLines().size
        assertTrue(
            "Restart should append to the same file, not create a new one",
            linesAfter > linesBefore
        )
        assertEquals(0, fileManager2.getPendingFiles().size)
    }

    @Test
    fun `enforceRetention obeys the 20 file cap across pending and uploading`() = runTest {
        val logsDir = File(tempFolder.root, "logs")
        val pendingDir = File(logsDir, "pending").apply { mkdirs() }
        val uploadingDir = File(logsDir, "uploading").apply { mkdirs() }
        val activeFile = File(logsDir, "active_log.txt")

        // 1. Plant 15 fake files in pending/
        (1..15).forEach { i ->
            File(pendingDir, "log_old_$i.txt").also { f ->
                f.writeText("fake log $i")
                f.setLastModified(i.toLong() * 1000)
            }
        }

        // 2. Move 10 into uploading/ (Total = 25 files, which is > 20 cap)
        val filesToMove = pendingDir.listFiles()?.take(10) ?: emptyList<File>()
        filesToMove.forEach { f -> f.renameTo(File(uploadingDir, f.name)) }

        // Combined total is now 25.
        // Rotation should trigger enforceRetention() and delete 5 oldest from pending/
        
        // Pre-fill active file past 512 KB to trigger rotation on next write
        activeFile.writeBytes(ByteArray(512 * 1024 + 1))

        fileManager.writeBatch(listOf(makeEvent("trigger rotation")))

        val finalPending = fileManager.getPendingFiles().size
        val finalUploading = uploadingDir.listFiles()?.size ?: 0

        assertTrue(
            "Expected pending ($finalPending) + uploading ($finalUploading) ≤ 20. Was ${finalPending + finalUploading}",
            finalPending + finalUploading <= 20
        )
        // Uploading files must NEVER be deleted by retention logic
        assertEquals("Uploading files must be untouched by retention", 10, finalUploading)
    }

    @Test
    fun `claimFileForUpload atomically moves file from pending to uploading`() = runTest {
        val bigMsg = "x".repeat(1024)
        fileManager.writeBatch(List(530) { makeEvent(bigMsg) })
        val pendingFile = fileManager.getPendingFiles().first()
        assertTrue("Pending file must exist before claim", pendingFile.exists())

        val uploadingFile = fileManager.claimFileForUpload(pendingFile)

        assertNotNull("claimFileForUpload should return the claimed file location", uploadingFile)
        assertFalse("Original pending file should no longer exist after claim", pendingFile.exists())
        assertTrue("Claimed file should exist in uploading/", uploadingFile!!.exists())
    }

    @Test
    fun `releaseFile moves file back from uploading to pending on upload failure`() = runTest {
        val bigMsg = "x".repeat(1024)
        fileManager.writeBatch(List(530) { makeEvent(bigMsg) })
        val pendingFile = fileManager.getPendingFiles().first()
        val fileName = pendingFile.name

        val uploadingFile = fileManager.claimFileForUpload(pendingFile)
        fileManager.releaseFile(uploadingFile!!)

        assertTrue(
            "Released file should be back in pending/ for retry",
            fileManager.getPendingFiles().any { it.name == fileName }
        )
        assertFalse("Released file must no longer exist in uploading/", uploadingFile.exists())
    }

    @Test
    fun `concurrent writeBatch calls do not corrupt file`() = runTest {
        val jobs = List(10) { i ->
            launch { fileManager.writeBatch(listOf(makeEvent("coroutine $i"))) }
        }
        jobs.joinAll()

        val lines = fileManager.readActiveFileLines()
        val lineCount = lines.count { it.isNotBlank() }
        assertEquals(10, lineCount)
    }
}
