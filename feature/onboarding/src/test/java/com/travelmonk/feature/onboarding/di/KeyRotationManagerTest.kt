package com.travelmonk.feature.onboarding.di

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val THIRTY_ONE_DAYS_MS = 31L * 24 * 3600 * 1000
private const val KEY_CREATED_AT = "master_key_created_at"

class KeyRotationManagerTest {

    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private val prefs: SharedPreferences = mockk()
    private val context: Context = mockk()
    private var fakeNow = 0L

    @Before
    fun setup() {
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.edit() } returns editor
    }

    private fun manager() = KeyRotationManager(context) { fakeNow }

    // ── shouldRotate ──────────────────────────────────────────────────────────

    @Test
    fun `shouldRotate returns false when no key record exists`() {
        every { prefs.getLong(KEY_CREATED_AT, 0L) } returns 0L

        assertFalse(manager().shouldRotate())
    }

    @Test
    fun `shouldRotate returns false when key is less than 30 days old`() {
        val oneDayMs = 24 * 3600 * 1000L
        fakeNow = oneDayMs * 2
        every { prefs.getLong(KEY_CREATED_AT, 0L) } returns oneDayMs

        assertFalse(manager().shouldRotate())
    }

    @Test
    fun `shouldRotate returns false at exactly 30 day boundary`() {
        val thirtyDaysMs = 30L * 24 * 3600 * 1000
        fakeNow = thirtyDaysMs + 1000L
        every { prefs.getLong(KEY_CREATED_AT, 0L) } returns 1000L

        assertFalse(manager().shouldRotate())
    }

    @Test
    fun `shouldRotate returns true when key is older than 30 days`() {
        fakeNow = THIRTY_ONE_DAYS_MS + 1000L
        every { prefs.getLong(KEY_CREATED_AT, 0L) } returns 1000L

        assertTrue(manager().shouldRotate())
    }

    // ── recordKeyCreation ─────────────────────────────────────────────────────

    @Test
    fun `recordKeyCreation stores current clock time`() {
        fakeNow = 1_716_000_000_000L

        manager().recordKeyCreation()

        verify { editor.putLong(KEY_CREATED_AT, 1_716_000_000_000L) }
    }

    // ── clearRecord ───────────────────────────────────────────────────────────

    @Test
    fun `clearRecord removes key creation timestamp`() {
        manager().clearRecord()

        verify { editor.remove(KEY_CREATED_AT) }
    }
}