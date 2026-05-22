package com.travelmonk.feature.onboarding.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.security.keystore.UserNotAuthenticatedException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.integration.android.AndroidKeystore
import com.travelmonk.core.logger.TravelMonkLogger
import com.travelmonk.feature.onboarding.data.local.SessionData
import com.travelmonk.feature.onboarding.data.local.SessionDataSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.inject.Singleton
import androidx.core.content.edit
import com.travelmonk.core.common.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val SESSION_FILE = "travelmonk_session.db"
private const val KEYSET_PREF_FILE = "travelmonk_tink_prefs"
private const val KEYSET_NAME = "travelmonk_session_keyset"
private const val MASTER_KEY_URI = "android-keystore://travelmonk_session_master_key"
private const val TAG = "SessionModule"

private const val KEY_META_PREF_FILE   = "travelmonk_key_meta"
private const val KEY_META_CREATED_AT  = "master_key_created_at"
private const val ROTATION_INTERVAL_MS = 30L * 24 * 3600 * 1000   // 30 days

/**
 * Tracks the creation timestamp of the Android Keystore master key in a separate
 * SharedPreferences file so rotation can be checked before the key itself is loaded.
 *
 * The metadata file ([KEY_META_PREF_FILE]) is excluded from both Auto Backup and
 * Cloud Backup — see backup_rules.xml and data_extraction_rules.xml.
 */
internal class KeyRotationManager(
    private val context: Context,
    private val clock: () -> Long = System::currentTimeMillis
) {

    fun shouldRotate(): Boolean {
        val createdAt = prefs().getLong(KEY_META_CREATED_AT, 0L)
        return createdAt > 0L && clock() - createdAt > ROTATION_INTERVAL_MS
    }

    fun recordKeyCreation() {
        prefs().edit { putLong(KEY_META_CREATED_AT, clock()) }
    }

    fun clearRecord() {
        prefs().edit { remove(KEY_META_CREATED_AT) }
    }

    private fun prefs() = context.getSharedPreferences(KEY_META_PREF_FILE, Context.MODE_PRIVATE)
}

/**
 * Thrown when the session master key cannot be confirmed as hardware-backed
 * on a real device. Callers should surface a security error rather than
 * continuing with a software key protecting sensitive session data.
 */
class HardwareSecurityException(message: String) : SecurityException(message)

/**
 * Thrown when the device is locked at the moment a session crypto operation is attempted.
 * This can happen if a background job (e.g. WorkManager) runs before the user unlocks
 * the device for the first time after boot. Callers must defer the operation until the
 * device is unlocked — do not crash or wipe session state.
 */
class DeviceLockedSessionException(message: String) : SecurityException(message)

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    /**
     * Provides a hardware-backed Tink AEAD primitive for encrypting session data.
     *
     * Strategy:
     *  1. Attempt StrongBox (dedicated HSM) if the device reports support.
     *  2. Fall back to TEE if StrongBox throws [StrongBoxUnavailableException] at runtime
     *     (can occur despite the PM feature flag due to firmware bugs or resource contention).
     *  3. Verify hardware backing on every cold start — not only at generation — so keys
     *     created by older installs without this check are also caught.
     *  4. Throw [HardwareSecurityException] on real devices if the key is software-only.
     */
    /**
     * Provides the session AEAD primitive as a [Deferred] so Android Keystore initialisation
     * (SharedPreferences reads, KeyStore.load, hardware-backing verification) runs entirely
     * on [Dispatchers.IO] without ever touching the main thread.
     *
     * The [Deferred] is started eagerly via [CoroutineScope.async] the moment Hilt builds the
     * singleton graph (Application.onCreate), so by the time [SessionDataSerializer] first
     * awaits it the result is virtually always ready — zero wait in the hot path.
     *
     * [SessionDataSerializer.readFrom] and [SessionDataSerializer.writeTo] are both suspend
     * functions and simply call [Deferred.await] before using the primitive.
     */
    @Provides
    @Singleton
    fun provideSessionAead(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): Deferred<@JvmSuppressWildcards Aead> = scope.async(Dispatchers.IO) {
        AeadConfig.register()

        val masterKeyAlias = MASTER_KEY_URI.removePrefix("android-keystore://")
        val supportsStrongBox = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

        val rotationManager = KeyRotationManager(context)

        if (!AndroidKeystore.hasKey(masterKeyAlias)) {
            generateMasterKey(masterKeyAlias, supportsStrongBox)
            rotationManager.recordKeyCreation()
        }

        // Rotate the master key if it is older than ROTATION_INTERVAL_MS (30 days).
        // This wipes the encrypted DataStore and regenerates the key so long-lived tokens
        // cannot be replayed from an old backup or a compromised key.
        if (rotationManager.shouldRotate()) {
            performKeyRotation(context, masterKeyAlias, supportsStrongBox, rotationManager)
        }

        // Verify on every cold start — not only at first generation.
        // Catches upgrades from older installs that may have created a software-backed
        // key before this verification existed.
        verifyHardwareBacking(masterKeyAlias)

        buildKeysetManager(context, masterKeyAlias, supportsStrongBox, rotationManager)
    }

    /**
     * Builds the Tink [AndroidKeysetManager], handling two failure modes introduced
     * by setUnlockedDeviceRequired and setInvalidatedByBiometricEnrollment:
     *
     * - [KeyPermanentlyInvalidatedException]: the master key was invalidated because new
     *   biometrics were enrolled after key creation. The key is gone forever — we wipe
     *   the keyset and DataStore, generate a fresh key, and let the user re-authenticate.
     *
     * - [UserNotAuthenticatedException]: the device is locked at the moment of access
     *   (possible if a background job runs before first unlock after boot). We throw
     *   [DeviceLockedSessionException] so callers can defer rather than crash.
     */
    private fun buildKeysetManager(
        context: Context,
        masterKeyAlias: String,
        supportsStrongBox: Boolean,
        rotationManager: KeyRotationManager
    ): Aead {
        fun buildAead() = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREF_FILE)
            .withKeyTemplate(AesGcmKeyManager.aes256GcmTemplate())
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)

        return try {
            buildAead()
        } catch (e: KeyPermanentlyInvalidatedException) {
            TravelMonkLogger.w(
                TAG,
                "Master key permanently invalidated (biometric enrollment changed) — " +
                    "wiping session and regenerating key: ${e.message}"
            )
            // Wipe everything that depended on the now-dead key.
            context.dataStoreFile(SESSION_FILE).delete()
            @Suppress("ApplySharedPref")
            context.getSharedPreferences(KEYSET_PREF_FILE, Context.MODE_PRIVATE)
                .edit(commit = true) { clear() } // commit() not apply() — must complete before key regeneration
            if (AndroidKeystore.hasKey(masterKeyAlias)) {
                AndroidKeystore.deleteKey(masterKeyAlias)
            }
            generateMasterKey(masterKeyAlias, supportsStrongBox)
            rotationManager.recordKeyCreation()
            // Second build will always succeed — fresh key, fresh keyset.
            buildAead()
        } catch (e: UserNotAuthenticatedException) {
            // Device is locked; key with setUnlockedDeviceRequired(true) cannot be used.
            // Do not wipe session — just signal to the caller to defer.
            throw DeviceLockedSessionException(
                "Session key unavailable: device is locked. Defer until device is unlocked. " +
                    "Cause: ${e.message}"
            )
        }
    }

    /**
     * Rotates the master key when it is older than [ROTATION_INTERVAL_MS] (30 days).
     *
     * Rotation procedure:
     *  1. Delete the encrypted DataStore file — its ciphertext is bound to the old keyset
     *     and cannot be decrypted after the key is gone anyway.
     *  2. Wipe the Tink keyset SharedPreferences — the keyset was encrypted with the old key.
     *  3. Delete the AndroidKeystore master key entry.
     *  4. Generate a fresh key and record its creation time.
     *
     * After rotation the DataStore is empty and [SessionData.isAuthenticated] is false —
     * the user is returned to the welcome screen and must re-authenticate via OTP.
     * This is intentional: session tokens should not outlive the master key.
     */
    private fun performKeyRotation(
        context: Context,
        alias: String,
        supportsStrongBox: Boolean,
        rotationManager: KeyRotationManager
    ) {
        TravelMonkLogger.i(TAG, "Master key rotation triggered (key is older than 30 days) — wiping session")
        @Suppress("ApplySharedPref")
        context.getSharedPreferences(KEYSET_PREF_FILE, Context.MODE_PRIVATE)
            .edit(commit = true) { clear() }                                     // synchronous — must complete before key deletion
        context.dataStoreFile(SESSION_FILE).delete()
        if (AndroidKeystore.hasKey(alias)) AndroidKeystore.deleteKey(alias)
        rotationManager.clearRecord()
        generateMasterKey(alias, supportsStrongBox)
        rotationManager.recordKeyCreation()
        TravelMonkLogger.i(TAG, "Key rotation complete — user must re-authenticate")
    }

    /**
     * Generates the master key, preferring StrongBox and falling back to TEE.
     *
     * [StrongBoxUnavailableException] can be thrown even when
     * [PackageManager.FEATURE_STRONGBOX_KEYSTORE] returns true — some devices have
     * buggy firmware or the chip may be temporarily unavailable. We catch it here
     * and retry with a plain TEE spec rather than crashing Hilt initialisation.
     */
    private fun generateMasterKey(alias: String, supportsStrongBox: Boolean) {
        if (supportsStrongBox) {
            try {
                AndroidKeystore.generateNewKeyWithSpec(buildKeySpec(alias, useStrongBox = true))
                TravelMonkLogger.i(TAG, "Generated StrongBox-backed master key")
                return
            } catch (e: StrongBoxUnavailableException) {
                TravelMonkLogger.w(
                    TAG,
                    "StrongBox unavailable at runtime despite feature flag; falling back to TEE: ${e.message}"
                )
            }
        }
        try {
            AndroidKeystore.generateNewKeyWithSpec(buildKeySpec(alias, useStrongBox = false))
            TravelMonkLogger.i(TAG, "Generated TEE-backed master key")
        } catch (e: ProviderException) {
            // KeyStoreException code 4 (ERROR_SYSTEM_ERROR) is thrown when
            // setUnlockedDeviceRequired(true) is requested on an emulator or on a real
            // device with no secure lock screen configured. Retry without that constraint
            // so the app does not crash — security is reduced but functional.
            TravelMonkLogger.w(
                TAG,
                "Key generation failed (no lock screen or emulator — code 4); " +
                    "retrying without unlock requirement: ${e.cause?.message}"
            )
            AndroidKeystore.generateNewKeyWithSpec(
                buildKeySpec(alias, useStrongBox = false, enforceUnlockPolicy = false)
            )
            TravelMonkLogger.w(TAG, "Generated master key without setUnlockedDeviceRequired — security is reduced")
        }
    }

    private fun buildKeySpec(
        alias: String,
        useStrongBox: Boolean,
        enforceUnlockPolicy: Boolean = true
    ): KeyGenParameterSpec =
        KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .apply {
                if (enforceUnlockPolicy) {
                    // Key is unusable while device is locked — defeats locked-device-in-hand attacks.
                    // Skipped when the device has no secure lock screen (code-4 fallback path).
                    setUnlockedDeviceRequired(true)
                    // Invalidate key permanently if new biometrics are enrolled after key creation.
                    setInvalidatedByBiometricEnrollment(true)
                }
            }
            .apply { if (useStrongBox) setIsStrongBoxBacked(true) }
            .build()

    /**
     * Verifies the key at [keyAlias] is hardware-backed (TEE or StrongBox).
     *
     * API split:
     * - API 31+: [KeyInfo.getSecurityLevel] with typed constants.
     * - API 28–30: deprecated [KeyInfo.isInsideSecureHardware] — the only option available.
     *
     * On a real device with a software-backed key, throws [HardwareSecurityException].
     * On an emulator (no TEE available), logs a warning and continues — this allows
     * dev and CI builds to function without hardware.
     */
    @Suppress("DEPRECATION")
    private fun verifyHardwareBacking(keyAlias: String) {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
        val secretKey = keyStore.getKey(keyAlias, null) as? SecretKey
            ?: throw HardwareSecurityException("Session master key '$keyAlias' not found in AndroidKeyStore")

        val factory = SecretKeyFactory.getInstance(secretKey.algorithm, "AndroidKeyStore")
        val keyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo

        val isHardwareBacked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT
                || keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX
        } else {
            keyInfo.isInsideSecureHardware
        }

        val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (keyInfo.securityLevel) {
                KeyProperties.SECURITY_LEVEL_STRONGBOX -> "StrongBox"
                KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> "TEE"
                else -> "Software (level=${keyInfo.securityLevel})"
            }
        } else {
            if (keyInfo.isInsideSecureHardware) "Hardware (TEE/StrongBox)" else "Software"
        }

        if (isHardwareBacked) {
            TravelMonkLogger.i(TAG, "Session master key is hardware-backed: $level")
            return
        }

        if (isEmulator()) {
            TravelMonkLogger.w(TAG, "Session master key is software-backed ($level) — emulator detected, continuing.")
        } else {
            throw HardwareSecurityException(
                "Session master key is not hardware-backed on a real device ($level). " +
                    "Refusing to use a software key for sensitive session data."
            )
        }
    }

    /**
     * Returns true for known Android emulator environments where no hardware TEE
     * is available. Used to allow software-backed keys in dev/CI without a real device.
     */
    private fun isEmulator(): Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("Emulator", ignoreCase = true) ||
            Build.MODEL.contains("Android SDK built for x86", ignoreCase = true) ||
            Build.MODEL.contains("sdk_gphone", ignoreCase = true) ||
            Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk_gphone", ignoreCase = true) ||
            Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
            Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))

    /**
     * Provides the encrypted proto-style [DataStore] backed by [SessionDataSerializer].
     *
     * [ReplaceFileCorruptionHandler] ensures a corrupted file (e.g. partial write on crash)
     * is replaced with [SessionData.EMPTY] rather than crashing — user simply re-authenticates.
     */
    @Provides
    @Singleton
    fun provideSessionDataStore(
        @ApplicationContext context: Context,
        aead: Deferred<@JvmSuppressWildcards Aead>,
        @ApplicationScope scope: CoroutineScope
    ): DataStore<SessionData> = DataStoreFactory.create(
        serializer = SessionDataSerializer(aead),
        corruptionHandler = ReplaceFileCorruptionHandler { SessionData.EMPTY },
        produceFile = { context.dataStoreFile(SESSION_FILE) },
        scope = scope
    )
}