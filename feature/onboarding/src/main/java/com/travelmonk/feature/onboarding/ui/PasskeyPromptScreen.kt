package com.travelmonk.feature.onboarding.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.feature.onboarding.R
import com.travelmonk.feature.onboarding.di.PASSKEY_TX_KEY_ALIAS
import com.travelmonk.feature.onboarding.di.PasskeyTransactionKeyHelper
import com.travelmonk.feature.onboarding.mvi.PasskeyPromptEffect
import com.travelmonk.feature.onboarding.mvi.PasskeyPromptIntent
import com.travelmonk.feature.onboarding.mvi.PasskeyPromptState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.security.Signature
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun PasskeyPromptScreen(
    viewModel: PasskeyPromptViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val txKeyHelper = remember { PasskeyTransactionKeyHelper(context) }

    // Concurrency safety: BaseViewModel emits effects via a RENDEZVOUS Channel, which means
    // the ViewModel's send() suspends until this collector receives the item. A second effect
    // cannot be emitted while launchRegistration / launchAuthentication is still suspending
    // inside collect { }. No external mutex is needed.
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PasskeyPromptEffect.LaunchPasskeyRegistration ->
                    launchRegistration(
                        context = context,
                        challengeJson = effect.challengeJson,
                        credentialManager = credentialManager,
                        txKeyHelper = txKeyHelper,
                        onAttestation = { viewModel.onIntent(PasskeyPromptIntent.AttestationObtained(it)) },
                        onCancelled = { viewModel.onIntent(PasskeyPromptIntent.CredentialCancelled) },
                        onError = { viewModel.onIntent(PasskeyPromptIntent.CredentialError(it)) }
                    )

                is PasskeyPromptEffect.LaunchPasskeyAuthentication ->
                    launchAuthentication(
                        context = context,
                        challengeJson = effect.challengeJson,
                        credentialManager = credentialManager,
                        txKeyHelper = txKeyHelper,
                        onAssertion = { viewModel.onIntent(PasskeyPromptIntent.AssertionObtained(it)) },
                        onCancelled = { viewModel.onIntent(PasskeyPromptIntent.CredentialCancelled) },
                        onError = { viewModel.onIntent(PasskeyPromptIntent.CredentialError(it)) }
                    )
            }
        }
    }

    PasskeyPromptContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun PasskeyPromptContent(
    state: PasskeyPromptState,
    onIntent: (PasskeyPromptIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.feature_onboarding_passkey_title),
                style = TravelMonkTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.feature_onboarding_passkey_subtitle),
                style = TravelMonkTheme.typography.bodyLarge,
                color = TravelMonkTheme.colors.onSurfaceVariant
            )
            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.error.asString(LocalContext.current),
                    color = TravelMonkTheme.colors.error,
                    style = TravelMonkTheme.typography.caption
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = { onIntent(PasskeyPromptIntent.RegisterPasskey) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(text = stringResource(R.string.feature_onboarding_create_passkey))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onIntent(PasskeyPromptIntent.SignInWithPasskey) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text(text = stringResource(R.string.feature_onboarding_sign_in_passkey))
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { onIntent(PasskeyPromptIntent.Skip) },
                enabled = !state.isLoading
            ) {
                Text(text = stringResource(R.string.feature_onboarding_skip))
            }
        }
    }
}

// ── Biometric error code constants ────────────────────────────────────────────
// android.hardware.biometrics.BiometricPrompt defines these as public fields, but their
// accessibility via Kotlin static reference varies across compile-SDK versions.
// Defining them here by value keeps the code readable without any API-level risk.
private const val BIOMETRIC_ERROR_USER_CANCELED = 10   // User tapped outside / swiped away
private const val BIOMETRIC_ERROR_NEGATIVE_BUTTON = 13 // User tapped the explicit Cancel button

// ── Credential Manager orchestration ─────────────────────────────────────────

/**
 * Runs the full passkey REGISTRATION ceremony:
 *  1. Calls [CredentialManager.createCredential] with the server challenge JSON.
 *  2. On success, signs the attestation JSON with the [PasskeyTransactionKeyHelper] transaction
 *     key via a [BiometricPrompt] (separate from the biometric CredentialManager shows internally).
 *  3. Delivers the signed wrapper payload via [onAttestation].
 *
 * Exception mapping:
 *  - [CreateCredentialCancellationException] → silent reset (user chose to cancel — normal UX)
 *  - [CreateCredentialInterruptedException] → transient failure, retry prompt
 *  - [NoCredentialException] → device/OS has no passkey provider (no GMS, restricted policy, etc.)
 *  - Other [CreateCredentialException] → generic registration failure
 *
 * Until backend integration the mock [challengeJson] from [PasskeyRepositoryImpl] is used.
 * When the backend lands, [onAttestation] receives the same wrapper format; the repository
 * unwraps `credential` + `txSig` and sends them to their respective endpoints.
 */
private suspend fun launchRegistration(
    context: Context,
    challengeJson: String,
    credentialManager: CredentialManager,
    txKeyHelper: PasskeyTransactionKeyHelper,
    onAttestation: (String) -> Unit,
    onCancelled: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val request = CreatePublicKeyCredentialRequest(challengeJson)
        val result = credentialManager.createCredential(context, request)
        val attestationJson = (result as CreatePublicKeyCredentialResponse).registrationResponseJson

        val payload = signAndWrap(context, txKeyHelper, attestationJson, onCancelled, onError)
            ?: return  // user cancelled the transaction key biometric — already notified

        onAttestation(payload)
    } catch (_: CreateCredentialCancellationException) {
        onCancelled()
    } catch (_: CreateCredentialInterruptedException) {
        onError(context.getString(R.string.feature_onboarding_error_passkey_interrupted))
    } catch (_: NoCredentialException) {
        onError(context.getString(R.string.feature_onboarding_error_passkey_no_provider))
    } catch (_: CreateCredentialException) {
        onError(context.getString(R.string.feature_onboarding_error_passkey_registration_failed))
    }
}

/**
 * Runs the full passkey AUTHENTICATION ceremony:
 *  1. Calls [CredentialManager.getCredential] with the server challenge JSON.
 *  2. On success, signs the assertion JSON with the transaction key.
 *  3. Delivers the signed wrapper via [onAssertion].
 *
 * [NoCredentialException] means the user has no passkeys registered on this device for this RP.
 * The correct UX guidance is to tap "Create Passkey" first or use "Skip for now".
 */
private suspend fun launchAuthentication(
    context: Context,
    challengeJson: String,
    credentialManager: CredentialManager,
    txKeyHelper: PasskeyTransactionKeyHelper,
    onAssertion: (String) -> Unit,
    onCancelled: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val request = GetCredentialRequest(
            listOf(GetPublicKeyCredentialOption(requestJson = challengeJson))
        )
        val result = credentialManager.getCredential(context, request)
        val assertionJson = (result.credential as PublicKeyCredential).authenticationResponseJson

        val payload = signAndWrap(context, txKeyHelper, assertionJson, onCancelled, onError)
            ?: return

        onAssertion(payload)
    } catch (_: GetCredentialCancellationException) {
        onCancelled()
    } catch (_: NoCredentialException) {
        onError(context.getString(R.string.feature_onboarding_error_passkey_no_credential))
    } catch (_: GetCredentialInterruptedException) {
        onError(context.getString(R.string.feature_onboarding_error_passkey_interrupted))
    } catch (_: GetCredentialException) {
        onError(context.getString(R.string.feature_onboarding_error_passkey_auth_failed))
    }
}

/**
 * Signs [credentialJson] with the [PasskeyTransactionKeyHelper] transaction key via
 * [BiometricPrompt] and returns the signed wrapper JSON, or null if the user cancelled.
 *
 * The transaction key has `setUserAuthenticationRequired(true)` + `validity=0`, which requires a
 * dedicated [BiometricPrompt.authenticate] call with a [BiometricPrompt.CryptoObject] wrapping
 * the [Signature]. This biometric is separate from the one CredentialManager shows internally —
 * the Keystore cannot use CredentialManager's internal biometric session.
 *
 * Resulting wrapper format:
 * ```json
 * {
 *   "credential": "<FIDO2 attestation or assertion JSON>",
 *   "txKeyAlias": "travelmonk_passkey_tx_key",
 *   "txSig": "<Base64-NO_WRAP ECDSA signature over the credential field>"
 * }
 * ```
 * When the backend lands, [PasskeyRepositoryImpl] will parse this wrapper and forward both
 * fields to their respective backend endpoints. The mock repository currently ignores [txSig]
 * and returns a fake token.
 */
private suspend fun signAndWrap(
    context: Context,
    txKeyHelper: PasskeyTransactionKeyHelper,
    credentialJson: String,
    onCancelled: () -> Unit,
    onError: (String) -> Unit
): String? {
    return try {
        val signature = txKeyHelper.getOrCreateSignature()
        val authenticatedSignature = performBiometricSigning(context, signature)
        val txSig = txKeyHelper.signData(authenticatedSignature, credentialJson.toByteArray(Charsets.UTF_8))
        buildSignedPayload(credentialJson, txSig)
    } catch (_: CancellationException) {
        onCancelled()
        null
    } catch (_: Exception) {
        onError(context.getString(R.string.feature_onboarding_error_passkey_tx_signing_failed))
        null
    }
}

/**
 * Bridges [BiometricPrompt.authenticate] (callback-based) to a coroutine via
 * [suspendCancellableCoroutine].
 *
 * The [signature] must be pre-initialised via [PasskeyTransactionKeyHelper.getOrCreateSignature].
 * After a successful biometric, the Keystore authorises exactly one signing operation on the
 * returned [Signature] instance. Pass it to [PasskeyTransactionKeyHelper.signData].
 *
 * Coroutine cancellation is propagated to the [CancellationSignal] so the prompt is dismissed
 * if the calling scope is cancelled (e.g. the user navigates away during the biometric dialog).
 *
 * Error codes 10 (USER_CANCELED) and 13 (NEGATIVE_BUTTON) are treated as user cancellations
 * and resume via [CancellationException]. All other error codes are surface as [SecurityException].
 *
   android.permission.USE_BIOMETRIC is declared in app/src/main/AndroidManifest.xml with
   normal protection level — granted automatically at install, no runtime request required.
   Lint fires here because the feature module manifest doesn't redeclare it; suppressed safely.
 */
@SuppressLint("MissingPermission")
private suspend fun performBiometricSigning(
    context: Context,
    signature: Signature
): Signature = suspendCancellableCoroutine { continuation ->
    val executor = context.mainExecutor
    val cancellationSignal = CancellationSignal()

    continuation.invokeOnCancellation { cancellationSignal.cancel() }

    val prompt = BiometricPrompt.Builder(context)
        .setTitle(context.getString(R.string.feature_onboarding_passkey_tx_biometric_title))
        .setSubtitle(context.getString(R.string.feature_onboarding_passkey_tx_biometric_subtitle))
        .setNegativeButton(
            context.getString(android.R.string.cancel),
            executor
        ) { _, _ ->
            continuation.cancel(CancellationException("User cancelled transaction key biometric"))
        }
        .build()

    prompt.authenticate(
        BiometricPrompt.CryptoObject(signature),
        cancellationSignal,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val sig = result.cryptoObject?.signature
                if (sig != null) {
                    continuation.resume(sig)
                } else {
                    continuation.resumeWithException(
                        IllegalStateException("BiometricPrompt returned null Signature in CryptoObject")
                    )
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // USER_CANCELED (10) = tapped outside / swiped away.
                // NEGATIVE_BUTTON (13) = tapped our explicit "Cancel" button.
                // Both are deliberate user decisions — treat as cancellation, not an error.
                if (errorCode == BIOMETRIC_ERROR_USER_CANCELED ||
                    errorCode == BIOMETRIC_ERROR_NEGATIVE_BUTTON
                ) {
                    continuation.cancel(CancellationException("User cancelled: $errString"))
                } else {
                    continuation.resumeWithException(
                        SecurityException("Biometric auth error [$errorCode]: $errString")
                    )
                }
            }

            override fun onAuthenticationFailed() {
                // Non-fatal: biometric attempt rejected (wrong finger, etc.). User can retry.
            }
        }
    )
}

/**
 * Builds the signed payload JSON wrapping the raw FIDO2 credential alongside the
 * transaction key signature.
 *
 * Uses [JSONObject] (built-in Android, no extra dependency) to guarantee correct escaping of
 * all Unicode code points and control characters (0x00–0x1F). The manual `.replace()` approach
 * is fragile and must not be used here.
 *
 * Schema (backend-stable):
 * ```json
 * {
 *   "credential" : "<FIDO2 attestation or assertion JSON — value-escaped by JSONObject>",
 *   "txKeyAlias" : "travelmonk_passkey_tx_key",
 *   "txSig"      : "<Base64-NO_WRAP ECDSA-P256 signature over the credential bytes>"
 * }
 * ```
 *
 * When the backend integration lands, [com.travelmonk.feature.onboarding.data.repository.PasskeyRepositoryImpl]
 * will parse this object and forward `credential` to the FIDO2 endpoint and `txSig` to the
 * transaction-key verification endpoint separately.
 */
private fun buildSignedPayload(credentialJson: String, txSig: String): String =
    JSONObject()
        .put("credential", credentialJson)
        .put("txKeyAlias", PASSKEY_TX_KEY_ALIAS)
        .put("txSig", txSig)
        .toString()

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PasskeyPromptContentPreview() {
    TravelMonkTheme {
        PasskeyPromptContent(
            state = PasskeyPromptState(),
            onIntent = {}
        )
    }
}