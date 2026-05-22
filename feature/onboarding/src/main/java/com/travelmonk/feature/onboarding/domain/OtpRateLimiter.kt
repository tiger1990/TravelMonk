package com.travelmonk.feature.onboarding.domain

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

/**
 * Client-side OTP request rate limiter.
 *
 * Allows at most [MAX_ATTEMPTS] calls to sendOtp per [WINDOW_MS] (5 minutes) per phone number.
 * On the 4th attempt within the window, returns [Result.Throttled] with the wait duration.
 *
 * This is a first line of defence to prevent accidental or intentional OTP flooding.
 * The backend must enforce its own rate limiting — this is not a substitute.
 *
 * State is in-memory and resets on process death (acceptable: backend is the authority).
 */
// Not annotated with @Inject directly — provided via AuthModule.provideOtpRateLimiter so that
// Hilt doesn't see two constructors (Kotlin emits a synthetic default constructor for each
// parameter with a default value, which violates Hilt's single-@Inject-constructor constraint).
@Singleton
class OtpRateLimiter(
    internal val clock: () -> Long
) {

    private val attemptsByPhone = ConcurrentHashMap<String, ArrayDeque<Long>>()

    sealed interface Result {
        data object Allowed : Result
        data class Throttled(val retryAfterMs: Long) : Result
    }

    /**
     * Checks whether [phone] is within the allowed rate and records the attempt if so.
     * Thread-safe: synchronized to prevent races across coroutines.
     */
    @Synchronized
    fun checkAndRecord(phone: String): Result {
        val now = clock()
        val attempts = attemptsByPhone.getOrPut(phone) { ArrayDeque() }

        // Drop attempts that have fallen outside the rolling window
        while (attempts.isNotEmpty() && now - attempts.first() > WINDOW_MS) {
            attempts.removeFirst()
        }

        return if (attempts.size >= MAX_ATTEMPTS) {
            val retryAfterMs = attempts.first() + WINDOW_MS - now
            Result.Throttled(retryAfterMs.coerceAtLeast(1_000L))
        } else {
            attempts.addLast(now)
            Result.Allowed
        }
    }

    companion object {
        internal const val MAX_ATTEMPTS = 3
        internal const val WINDOW_MS = 5L * 60 * 1_000 // 5 minutes
    }
}