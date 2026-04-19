package com.travelmonk.core.logger.upload

import com.travelmonk.core.logger.LogEvent
import com.travelmonk.core.logger.LogLevel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CriticalEventUploaderTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private fun makeEvent(msg: String = "error occurred") = LogEvent(
        timestamp = System.currentTimeMillis(),
        level = LogLevel.ERROR,
        tag = "TestTag",
        message = msg
    )

    @Test
    fun `sendCritical is called for each event in channel`() = testScope.runTest {
        val channel = Channel<LogEvent>(capacity = 10)
        val sender = mockk<RemoteLogSender>()
        coEvery { sender.sendCritical(any()) } returns UploadResult.Success

        CriticalEventUploader(channel, sender).start(this)

        val event1 = makeEvent("first")
        val event2 = makeEvent("second")
        channel.send(event1)
        channel.send(event2)
        channel.close()

        coVerify(exactly = 1) { sender.sendCritical(event1) }
        coVerify(exactly = 1) { sender.sendCritical(event2) }
    }

    @Test
    fun `network failure does not crash the uploader`() = testScope.runTest {
        val channel = Channel<LogEvent>(capacity = 10)
        val sender = mockk<RemoteLogSender>()
        coEvery { sender.sendCritical(any()) } throws RuntimeException("network dead")

        CriticalEventUploader(channel, sender).start(this)

        val event = makeEvent()
        channel.send(event)
        channel.close()

        // No exception propagated — uploader swallows it
        coVerify(exactly = 1) { sender.sendCritical(event) }
    }

    @Test
    fun `uploader handles empty channel without error`() = testScope.runTest {
        val channel = Channel<LogEvent>(capacity = 10)
        val sender = mockk<RemoteLogSender>()

        CriticalEventUploader(channel, sender).start(this)
        channel.close()

        coVerify(exactly = 0) { sender.sendCritical(any()) }
    }

    @Test
    fun `uploader continues processing after individual event failure`() = testScope.runTest {
        val channel = Channel<LogEvent>(capacity = 10)
        val sender = mockk<RemoteLogSender>()

        var callCount = 0
        coEvery { sender.sendCritical(any()) } answers {
            callCount++
            if (callCount == 1) throw RuntimeException("first fails")
            UploadResult.Success
        }

        CriticalEventUploader(channel, sender).start(this)

        channel.send(makeEvent("first"))
        channel.send(makeEvent("second"))
        channel.close()

        coVerify(exactly = 2) { sender.sendCritical(any()) }
    }
}
