package com.travelmonk.feature.onboarding.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import com.travelmonk.core.logger.TravelMonkLogger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec

internal const val PASSKEY_TX_KEY_ALIAS = "travelmonk_passkey_tx_key"
private const val TAG = "PasskeyTxKey"

/**
 * Manages the passkey transaction key — a second Android Keystore key used exclusively
 * to sign passkey assertions and attestations immediately after user biometric authentication.
 *
 * ## Why a separate transaction key?
 *
 * The FIDO2 credential managed by CredentialManager proves the user via biometrics internally,
 * but that proof is scoped to the FIDO2 protocol and is not accessible to the app. The transaction
 * key provides an additional cryptographic binding: the signature proves that this specific app
 * installation on this specific device authorised the passkey operation, even if an attacker
 * intercepts the assertion/attestation JSON on the wire.
 *
 * The backend verifies both:
 *  1. The FIDO2 assertion/attestation (standard WebAuthn)
 *  2. The transaction key signature (TravelMonk-specific app binding)
 *
 * ## Key policy
 *
 * | Property | Value | Rationale |
 * |----------|-------|-----------|
 * | Algorithm | EC / secp256r1 | Matches FIDO2 key type; small output, fast verify |
 * | Digest | SHA-256 | Standard for ECDSA in WebAuthn context |
 * | Purpose | PURPOSE_SIGN | Signing only — no encryption |
 * | setUserAuthenticationRequired | true | Requires biometric before each use |
 * | setUserAuthenticationValidityDurationSeconds | 0 | Per-operation — mandatory for StrongBox |
 * | setInvalidatedByBiometricEnrollment | true | New biometric enrollment voids the key |
 * | Hardware backing | StrongBox → TEE fallback | Matches DataStore master key policy |
 *
 * ## Why validity=0 (not a time window)
 *
 * StrongBox (Titan M, etc.) has no access to trusted wall-clock time from the AP, so it rejects
 * any `validity > 0`. We use `validity=0` so the same spec works on StrongBox and TEE devices.
 * The consequence: every signing operation requires a prior [android.hardware.biometrics.BiometricPrompt]
 * call with a [android.hardware.biometrics.BiometricPrompt.CryptoObject] wrapping this [Signature].
 *
 * ## Signing flow
 *
 * ```
 * 1. getOrCreateSignature()     — obtain Signature, wrap in BiometricPrompt.CryptoObject
 * 2. BiometricPrompt.authenticate(CryptoObject, cancellationSignal, executor, callback)
 * 3. onAuthenticationSucceeded: result.cryptoObject?.signature (now Keystore-authorised)
 * 4. signData(authenticatedSignature, payload)  — signs and returns Base64 string
 * ```
 *
 * This class is [internal] so it is accessible from tests in the same Gradle module.
 */
internal class PasskeyTransactionKeyHelper(private val context: Context) {

    private val keyStore: KeyStore =
        KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }

    /**
     * Returns a [Signature] pre-initialised with the transaction private key for signing.
     *
     * Wrap the returned [Signature] in `BiometricPrompt.CryptoObject` and pass it to
     * `BiometricPrompt.authenticate(...)`. After successful authentication, the Keystore
     * permits one signing operation on this [Signature] instance.
     *
     * Generates the EC key pair on first call if it does not yet exist.
     */
    fun getOrCreateSignature(): Signature {
        if (!keyStore.containsAlias(PASSKEY_TX_KEY_ALIAS)) {
            generateTransactionKey()
        }
        val privateKey = keyStore.getKey(PASSKEY_TX_KEY_ALIAS, null) as? PrivateKey
            ?: error("Transaction key '$PASSKEY_TX_KEY_ALIAS' not found after generation")
        return Signature.getInstance("SHA256withECDSA").apply { initSign(privateKey) }
    }

    /**
     * Signs [data] using the [Signature] that was authorised by `BiometricPrompt`.
     *
     * Must be called inside `BiometricPrompt.AuthenticationCallback.onAuthenticationSucceeded`
     * using `result.cryptoObject?.signature` from the callback result.
     *
     * @return Base64 (NO_WRAP) encoded DER-encoded ECDSA signature bytes
     */
    fun signData(signature: Signature, data: ByteArray): String {
        signature.update(data)
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
    }

    /**
     * Deletes the transaction key entry from the Keystore.
     *
     * Called alongside DataStore master key rotation so both keys are renewed together.
     * The key is regenerated lazily on the next [getOrCreateSignature] call.
     */
    fun clearKey() {
        if (keyStore.containsAlias(PASSKEY_TX_KEY_ALIAS)) {
            keyStore.deleteEntry(PASSKEY_TX_KEY_ALIAS)
            TravelMonkLogger.i(TAG, "Passkey transaction key cleared")
        }
    }

    private fun generateTransactionKey() {
        val supportsStrongBox = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

        if (supportsStrongBox) {
            try {
                generateKeyPairWithSpec(useStrongBox = true)
                TravelMonkLogger.i(TAG, "Generated StrongBox-backed passkey transaction key")
                return
            } catch (e: StrongBoxUnavailableException) {
                TravelMonkLogger.w(
                    TAG,
                    "StrongBox unavailable for passkey transaction key; falling back to TEE: ${e.message}"
                )
            }
        }
        generateKeyPairWithSpec(useStrongBox = false)
        TravelMonkLogger.i(TAG, "Generated TEE-backed passkey transaction key")
    }

    private fun generateKeyPairWithSpec(useStrongBox: Boolean) {
        val builder = KeyGenParameterSpec.Builder(PASSKEY_TX_KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .apply { if (useStrongBox) setIsStrongBoxBacked(true) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(0)
        }

        val spec = builder.build()

        KeyPairGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
            .apply { initialize(spec) }
            .generateKeyPair()
    }
}