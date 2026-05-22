package com.travelmonk.feature.onboarding.data.local

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.travelmonk.core.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SessionDataSerializerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var aead: Aead
    private lateinit var serializer: SessionDataSerializer

    @Before
    fun setup() {
        AeadConfig.register()
        @Suppress("DEPRECATION")
        val keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM)
        aead = keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        // Wrap in CompletableDeferred so the test Aead is already resolved when awaited
        serializer = SessionDataSerializer(CompletableDeferred(aead))
    }

    // ── Round-trip ────────────────────────────────────────────────────────────

    @Test
    fun `write then read returns original SessionData`() = runTest {
        val original = SessionData(
            accessToken = "access_abc",
            refreshToken = "refresh_xyz",
            userId = "usr_001",
            phoneNumber = "+919876543210",
            passkeyRegistered = true,
            onboardingComplete = true
        )

        val output = ByteArrayOutputStream()
        serializer.writeTo(original, output)
        val result = serializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, result)
    }

    @Test
    fun `isAuthenticated true after full round-trip with valid fields`() = runTest {
        val data = SessionData(
            accessToken = "access_abc",
            userId = "usr_001",
            onboardingComplete = true
        )

        val output = ByteArrayOutputStream()
        serializer.writeTo(data, output)
        val result = serializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assert(result.isAuthenticated)
    }

    // ── Empty input ───────────────────────────────────────────────────────────

    @Test
    fun `empty input stream returns SessionData EMPTY`() = runTest {
        val result = serializer.readFrom(ByteArrayInputStream(ByteArray(0)))

        assertEquals(SessionData.EMPTY, result)
    }

    @Test
    fun `empty input does not throw`() = runTest {
        val result = runCatching {
            serializer.readFrom(ByteArrayInputStream(ByteArray(0)))
        }
        assert(result.isSuccess)
    }

    // ── Tamper detection ──────────────────────────────────────────────────────

    @Test
    fun `tampered ciphertext returns SessionData EMPTY`() = runTest {
        val data = SessionData(
            accessToken = "access_abc",
            userId = "usr_001",
            onboardingComplete = true
        )
        val output = ByteArrayOutputStream()
        serializer.writeTo(data, output)

        val tampered = output.toByteArray()
        tampered[tampered.size / 2] = tampered[tampered.size / 2].inc()

        val result = serializer.readFrom(ByteArrayInputStream(tampered))

        assertEquals(SessionData.EMPTY, result)
    }

    @Test
    fun `tampered ciphertext does not throw`() = runTest {
        val output = ByteArrayOutputStream()
        serializer.writeTo(SessionData(accessToken = "access_abc", userId = "usr_001"), output)
        val tampered = output.toByteArray().also { it[it.size / 2] = it[it.size / 2].inc() }

        val result = runCatching {
            serializer.readFrom(ByteArrayInputStream(tampered))
        }
        assert(result.isSuccess)
    }

    // ── Default value ─────────────────────────────────────────────────────────

    @Test
    fun `default value is SessionData EMPTY`() {
        assertEquals(SessionData.EMPTY, serializer.defaultValue)
    }

    @Test
    fun `SessionData EMPTY is not authenticated`() {
        assertFalse(SessionData.EMPTY.isAuthenticated)
    }
}
