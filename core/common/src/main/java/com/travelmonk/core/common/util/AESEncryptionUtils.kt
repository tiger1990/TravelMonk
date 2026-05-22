package com.travelmonk.core.common.util

import android.util.Base64
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * A secure, production-ready utility for AES encryption on Android using GCM.
 *
 * ## Why AES-GCM?
 * This implementation uses **AES/GCM/NoPadding**, which is an Authenticated Encryption 
 * with Associated Data (AEAD) mode. Unlike older modes like CBC, GCM provides both:
 * 1. **Confidentiality**: Only those with the key can read the data.
 * 2. **Integrity**: Any attempt to tamper with the encrypted data will cause decryption to fail.
 *
 * ## Implementation Details:
 * - **Algorithm**: AES/GCM/NoPadding
 * - **IV Length**: 12 bytes (standard for GCM)
 * - **Tag Length**: 128 bits
 * - **Encoding**: UTF-8 for strings, Base64 (NO_WRAP) for final output.
 * - **Storage Strategy**: The 12-byte IV is prepended to the ciphertext.
 *
 * ## Security Best Practices:
 * - Use the **Android Keystore** to generate and manage the [ByteArray] keys passed to these methods.
 * - Never hardcode encryption keys in source code.
 * - For extremely sensitive data on API 33+, ensure your Keystore keys are **StrongBox-backed**.
 *
 * @author TravelMonk Security Team
 */
object AESEncryptionUtils {

    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val ALGORITHM = "AES/GCM/NoPadding"

    /**
     * Encrypts the given [plainText] using the provided [key].
     *
     * @param plainText The string to encrypt.
     * @param key The 128, 192, or 256-bit AES key. **Must be sourced from Android Keystore.**
     *   Never pass a hardcoded or statically-derived byte array — doing so moves the security
     *   boundary out of hardware and into the app process, defeating the protection this
     *   utility is designed to provide. Obtain the key bytes from a [javax.crypto.SecretKey]
     *   retrieved from [java.security.KeyStore] with provider "AndroidKeyStore".
     * @return A Base64-encoded string containing the IV and ciphertext.
     * @throws GeneralSecurityException if encryption fails.
     */
    fun encrypt(plainText: String, key: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val secretKey: SecretKey = SecretKeySpec(key, "AES")

        // Android's provider generates a cryptographically secure random IV on init
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv

        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Prepend IV (12 bytes) to the encrypted data
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts a Base64 string produced by [encrypt].
     *
     * @param encryptedBase64 The Base64 string to decrypt.
     * @param key The same key used for encryption. **Must be sourced from Android Keystore.**
     *   Never pass a hardcoded or statically-derived byte array. See [encrypt] for details.
     * @return The original plain text.
     * @throws IllegalArgumentException if the input format is invalid.
     * @throws GeneralSecurityException if decryption or authentication fails
     *   (includes GCM tag mismatch indicating tampering or wrong key).
     */
    fun decrypt(encryptedBase64: String, key: ByteArray): String {
        val combined = try {
            Base64.decode(encryptedBase64, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Base64 input", e)
        }

        if (combined.size < GCM_IV_LENGTH) {
            throw IllegalArgumentException("Invalid ciphertext: too short")
        }

        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(ALGORITHM)
        val secretKey: SecretKey = SecretKeySpec(key, "AES")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedBytes = cipher.doFinal(ciphertext)

        return String(decryptedBytes, Charsets.UTF_8)
    }
}
