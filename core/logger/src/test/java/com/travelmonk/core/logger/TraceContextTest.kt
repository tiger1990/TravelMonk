package com.travelmonk.core.logger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TraceContextTest {

    @After
    fun resetDebugMode() {
        TraceContext.debugMode = false
    }

    @Test
    fun `current returns null outside coroutine context`() {
        assertNull(TraceContext.current())
    }

    @Test
    fun `current returns trace inside withContext`() = runTest {
        val trace = TraceContext.new()
        withContext(trace) {
            assertNotNull(TraceContext.current())
            assertEquals(trace.traceId, TraceContext.current()!!.traceId)
        }
    }

    @Test
    fun `current is restored to null after withContext exits`() = runTest {
        val trace = TraceContext.new()
        withContext(trace) {
            // inside — trace is set
        }
        // outside — should be null again
        assertNull(TraceContext.current())
    }

    @Test
    fun `child coroutine shares parent trace`() = runTest(UnconfinedTestDispatcher()) {
        val trace = TraceContext.new()
        var childTraceId: String? = null

        withContext(trace) {
            val job = launch {
                childTraceId = TraceContext.current()?.traceId
            }
            job.join()
        }

        assertEquals(trace.traceId, childTraceId)
    }

    @Test
    fun `trace propagates across dispatcher switch`() = runTest(UnconfinedTestDispatcher()) {
        val trace = TraceContext.new()
        var observedTraceId: String? = null

        withContext(trace) {
            withContext(Dispatchers.Default) {
                observedTraceId = TraceContext.current()?.traceId
            }
        }

        assertEquals(trace.traceId, observedTraceId)
    }

    @Test
    fun `async child shares parent trace`() = runTest(UnconfinedTestDispatcher()) {
        val trace = TraceContext.new()

        val result = withContext(trace) {
            async { TraceContext.current()?.traceId }.await()
        }

        assertEquals(trace.traceId, result)
    }

    @Test
    fun `new generates unique traceId and spanId each call`() {
        val a = TraceContext.new()
        val b = TraceContext.new()

        assertNotEquals(a.traceId, b.traceId)
        assertNotEquals(a.spanId, b.spanId)
    }

    @Test
    fun `traceId and spanId are different within same TraceContext`() {
        val trace = TraceContext.new()
        assertNotEquals(trace.traceId, trace.spanId)
    }

    @Test
    fun `nested withContext restores outer trace on exit`() = runTest(UnconfinedTestDispatcher()) {
        val outer = TraceContext.new()
        val inner = TraceContext.new()
        var traceAfterInner: String? = null

        withContext(outer) {
            withContext(inner) {
                // inner is active here
            }
            traceAfterInner = TraceContext.current()?.traceId
        }

        assertEquals(outer.traceId, traceAfterInner)
    }

    // --- flow + launchStack tests ---

    @Test
    fun `flow is null by default when not specified in new()`() {
        val trace = TraceContext.new()
        assertNull(trace.flow)
    }

    @Test
    fun `specified flow is set on the TraceContext`() {
        val trace = TraceContext.new(flow = "LoginFlow")
        assertEquals("LoginFlow", trace.flow)
    }

    @Test
    fun `flow propagates to child coroutines`() = runTest(UnconfinedTestDispatcher()) {
        val trace = TraceContext.new(flow = "SearchFlow")
        var childFlow: String? = null

        withContext(trace) {
            launch { childFlow = TraceContext.current()?.flow }.join()
        }

        assertEquals("SearchFlow", childFlow)
    }

    @Test
    fun `flow propagates across dispatcher switch`() = runTest(UnconfinedTestDispatcher()) {
        val trace = TraceContext.new(flow = "BookingFlow")
        var observedFlow: String? = null

        withContext(trace) {
            withContext(Dispatchers.Default) {
                observedFlow = TraceContext.current()?.flow
            }
        }

        assertEquals("BookingFlow", observedFlow)
    }

    @Test
    fun `launchStack is null when debugMode is false`() {
        TraceContext.debugMode = false
        val trace = TraceContext.new()
        assertNull(trace.launchStack)
    }

    @Test
    fun `launchStack is non-null when debugMode is true`() {
        TraceContext.debugMode = true
        val trace = TraceContext.new()
        assertNotNull(trace.launchStack)
    }

    @Test
    fun `launchStack contains recognisable caller info`() {
        TraceContext.debugMode = true
        val trace = TraceContext.new()
        // The stack string must reference this test class
        assertTrue(
            "Expected launchStack to mention TraceContextTest but was: ${trace.launchStack}",
            trace.launchStack?.contains("TraceContextTest") == true
        )
    }
}
