package com.travelmonk.core.permissions

/**
 * Represents the runtime permission state for a single Android permission.
 *
 * Use [rememberTravelMonkPermission] to observe the state in Compose and obtain a lambda
 * that triggers the system permission dialog.
 *
 * ## State machine
 *
 * ```
 *   First launch (never requested)
 *         │
 *         ▼
 *      [Denied] ──── user taps "Allow" ──────────────────► [Granted]
 *         │
 *         │  OS sets shouldShowRationale = true after first denial
 *         ▼
 *   [ShowRationale] ── user confirms ──► request again ──► [Granted]
 *                                                │
 *                                                └── [Denied] again
 *
 *   At any point: user taps "Don't ask again"
 *         │
 *         ▼
 *   [PermanentlyDenied] ── only Settings can resolve this
 * ```
 *
 * ## Typical usage
 *
 * ```kotlin
 * val (cameraState, requestCamera) = rememberTravelMonkPermission(Manifest.permission.CAMERA)
 *
 * when (cameraState) {
 *     is PermissionState.Granted           -> QrScannerContent()
 *     is PermissionState.ShowRationale     -> PermissionRationaleDialog(
 *                                                title     = stringResource(R.string.camera_rationale_title),
 *                                                body      = stringResource(R.string.camera_rationale_body),
 *                                                onConfirm = cameraState.onConfirm,
 *                                                onDismiss = { /* handle dismiss */ }
 *                                            )
 *     is PermissionState.PermanentlyDenied -> OpenSettingsPrompt()
 *     is PermissionState.Denied            -> Button(onClick = requestCamera) {
 *                                                Text(stringResource(R.string.allow_camera))
 *                                            }
 * }
 * ```
 */
sealed interface PermissionState {

    /**
     * The permission has been granted. Safe to access the protected feature.
     */
    data object Granted : PermissionState

    /**
     * The permission was previously denied and the OS set `shouldShowRationale = true`.
     * Show the user an explanation before requesting again.
     *
     * Call [onConfirm] after the user acknowledges the rationale — it re-triggers the system
     * permission dialog. Typically passed directly to [PermissionRationaleDialog.onConfirm].
     */
    data class ShowRationale(val onConfirm: () -> Unit) : PermissionState

    /**
     * The permission has not been granted and no prior denial is recorded (first launch), or
     * the user dismissed without choosing. Call the request lambda from [rememberTravelMonkPermission]
     * to show the system dialog.
     */
    data object Denied : PermissionState

    /**
     * The user selected "Don't ask again". The system dialog will no longer appear.
     * Guide the user to **Settings → App → Permissions** via
     * [android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS].
     */
    data object PermanentlyDenied : PermissionState
}
