package com.travelmonk.feature.onboarding.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val PHONE = "+919876543210"
private const val OTHER_PHONE = "+14155552671"

class OtpRateLimiterTest {

    private var fakeNow = 0L
    private fun limiter() = OtpRateLimiter { fakeNow }

    // ── Within limit ──────────────────────────────────────────────────────────

    @Test
    fun `first attempt is allowed`() {
        val result = limiter().checkAndRecord(PHONE)
        assertEquals(OtpRateLimiter.Result.Allowed, result)
    }

    @Test
    fun `second attempt is allowed`() {
        val l = limiter()
        l.checkAndRecord(PHONE)
        val result = l.checkAndRecord(PHONE)
        assertEquals(OtpRateLimiter.Result.Allowed, result)
    }

    @Test
    fun `third attempt is allowed`() {
        val l = limiter()
        repeat(2) { l.checkAndRecord(PHONE) }
        val result = l.checkAndRecord(PHONE)
        assertEquals(OtpRateLimiter.Result.Allowed, result)
    }

    // ── Rate limit enforced ───────────────────────────────────────────────────

    @Test
    fun `fourth attempt within window is throttled`() {
        val l = limiter()
        repeat(OtpRateLimiter.MAX_ATTEMPTS) { l.checkAndRecord(PHONE) }
        val result = l.checkAndRecord(PHONE)
        assertTrue(result is OtpRateLimiter.Result.Throttled)
    }

    @Test
    fun `throttled result has positive retryAfterMs`() {
        val l = limiter()
        repeat(OtpRateLimiter.MAX_ATTEMPTS) { l.checkAndRecord(PHONE) }
        val result = l.checkAndRecord(PHONE) as OtpRateLimiter.Result.Throttled
        assertTrue(result.retryAfterMs > 0)
    }

    // ── Window expiry resets the limit ────────────────────────────────────────

    @Test
    fun `attempt is allowed again after window expires`() {
        val l = limiter()
        repeat(OtpRateLimiter.MAX_ATTEMPTS) { l.checkAndRecord(PHONE) }

        // Advance clock past the 5-minute window
        fakeNow = OtpRateLimiter.WINDOW_MS + 1_000L

        val result = l.checkAndRecord(PHONE)
        assertEquals(OtpRateLimiter.Result.Allowed, result)
    }

    // ── Per-phone isolation ───────────────────────────────────────────────────

    @Test
    fun `different phones have independent limits`() {
        val l = limiter()
        repeat(OtpRateLimiter.MAX_ATTEMPTS) { l.checkAndRecord(PHONE) }

        val result = l.checkAndRecord(OTHER_PHONE)
        assertEquals(OtpRateLimiter.Result.Allowed, result)
    }
}