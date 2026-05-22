package com.travelmonk.feature.onboarding.data.local

import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import kotlinx.coroutines.Deferred
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * DataStore [Serializer] for [SessionData] with Tink AEAD encryption.
 *
 * Write path: SessionData → JSON bytes → AEAD encrypt → disk
 * Read path:  disk → AEAD decrypt → JSON bytes → SessionData
 *
 * [aead] is a [Deferred] so the Android Keystore initialisation (SharedPreferences reads,
 * KeyStore.load, hardware-backing verification) can complete on an IO thread independently
 * of the main thread. Both [readFrom] and [writeTo] are already suspend — they simply
 * [Deferred.await] the primitive before using it. By the time DataStore first performs
 * a real read or write, the Deferred is virtually always resolved.
 *
 * The [ASSOCIATED_DATA] constant binds the ciphertext to this specific DataStore file,
 * preventing an attacker from swapping ciphertexts between different encrypted stores
 * (an AEAD property — decryption fails if associated data doesn't match).
 *
 * On any decryption or deserialization failure (corrupted file, key rotation, tampering)
 * [defaultValue] is returned so the app degrades gracefully: the user simply
 * re-authenticates rather than crashing.
 */
internal class SessionDataSerializer(private val aead: Deferred<@JvmSuppressWildcards Aead>) : Serializer<SessionData> {

    override val defaultValue: SessionData = SessionData.EMPTY

    override suspend fun readFrom(input: InputStream): SessionData {
        val encryptedBytes = input.readBytes()
        if (encryptedBytes.isEmpty()) return defaultValue
        return try {
            val plainBytes = aead.await().decrypt(encryptedBytes, ASSOCIATED_DATA)
            Json.decodeFromString(SessionData.serializer(), String(plainBytes, Charsets.UTF_8))
        } catch (_: Exception) {
            // Corrupted or tampered — return empty; user will re-authenticate
            defaultValue
        }
    }

    override suspend fun writeTo(t: SessionData, output: OutputStream) {
        val plainBytes = Json.encodeToString(SessionData.serializer(), t).toByteArray(Charsets.UTF_8)
        val encryptedBytes = aead.await().encrypt(plainBytes, ASSOCIATED_DATA)
        output.write(encryptedBytes)
    }

    companion object {
        private val ASSOCIATED_DATA = "travelmonk_session".toByteArray(Charsets.UTF_8)
    }
}