package com.travelmonk.core.logger

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LogFileManagerTest {

    private lateinit var fileManager: LogFileManager

    @Before
    fun setup() {
        fileManager = LogFileManager(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `writeBatch writes lines to active file`() {
        fileManager.writeBatch(listOf("{\"msg\":\"hello\"}\n", "{\"msg\":\"world\"}\n"))

        val content = fileManager.getActiveFile().readText()
        assertTrue(content.contains("hello"))
        assertTrue(content.contains("world"))
    }

    @Test
    fun `writeBatch rotates file when size exceeds 512KB`() {
        val bigLine = "x".repeat(1024) + "\n"
        fileManager.writeBatch(List(530) { bigLine }) // ~530 KB

        assertTrue(
            "File should rotate into pending/",
            fileManager.getPendingFiles().isNotEmpty()
        )
    }

    @Test
    fun `rotationEvents emits when file rotates`() = runTest {
        var emitted = false
        val job = launch(UnconfinedTestDispatcher()) {
            fileManager.rotationEvents.collect { emitted = true }
        }

        val bigLine = "x".repeat(1024) + "\n"
        fileManager.writeBatch(List(530) { bigLine })

        assertTrue("rotationEvents should emit on rotation", emitted)
        job.cancel()
    }

    @Test
    fun `getPendingFiles is empty before any rotation`() {
        assertTrue(fileManager.getPendingFiles().isEmpty())
    }

    @Test
    fun `deleteAfterUpload removes file from pending`() {
        val bigLine = "x".repeat(1024) + "\n"
        fileManager.writeBatch(List(530) { bigLine })

        val pending = fileManager.getPendingFiles()
        assertTrue(pending.isNotEmpty())

        val file = pending.first()
        fileManager.deleteAfterUpload(file)
        assertFalse("Deleted file should no longer exist", file.exists())
    }

    @Test
    fun `app restart resumes writing to same file when under 512KB`() {
        fileManager.writeBatch(listOf("{\"msg\":\"session1\"}\n"))
        val sizeBefore = fileManager.getActiveFile().length()

        // Simulate app restart — new LogFileManager instance, same context
        val fileManager2 = LogFileManager(RuntimeEnvironment.getApplication())
        fileManager2.writeBatch(listOf("{\"msg\":\"session2\"}\n"))

        assertTrue(
            "Restart should append to the same file, not create a new one",
            fileManager2.getActiveFile().length() > sizeBefore
        )
        assertEquals(0, fileManager2.getPendingFiles().size)
    }

    @Test
    fun `concurrent writeBatch calls do not corrupt file`() {
        val threads = List(10) {
            Thread { fileManager.writeBatch(listOf("{\"thread\":$it}\n")) }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }

        val content = fileManager.getActiveFile().readText()
        val lineCount = content.lines().count { it.isNotBlank() }
        assertEquals(10, lineCount)
    }
}
