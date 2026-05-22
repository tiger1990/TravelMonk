package com.travelmonk.core.permissions

import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Observes the runtime permission state for [permission] and returns the current
 * [PermissionState] alongside a lambda that opens the system permission dialog.
 *
 * No Accompanist dependency — implemented directly on top of stable
 * [ActivityResultContracts.RequestPermission] (AndroidX Activity).
 *
 * ## How it works
 *
 * 1. On first composition the current grant state is resolved synchronously via
 *    [ContextCompat.checkSelfPermission] — no dialog is shown.
 * 2. [ActivityCompat.shouldShowRequestPermissionRationale] distinguishes
 *    [PermissionState.Denied] (first launch, request is appropriate) from
 *    [PermissionState.ShowRationale] (previous denial; explain before requesting again).
 * 3. After the system dialog resolves, the returned [PermissionState] is updated and
 *    [onResult] is invoked with the new state.
 * 4. When [PermissionState.ShowRationale] is active, [PermissionState.ShowRationale.onConfirm]
 *    is pre-wired to re-trigger the launcher — the caller only needs to call `onConfirm()`
 *    without holding a launcher reference.
 *
 * ## Usage
 *
 * ```kotlin
 * @Composable
 * fun CameraScreen() {
 *     val (cameraState, requestCamera) = rememberTravelMonkPermission(
 *         permission = Manifest.permission.CAMERA
 *     )
 *
 *     when (cameraState) {
 *         is PermissionState.Granted -> {
 *             QrScannerContent()
 *         }
 *         is PermissionState.ShowRationale -> {
 *             PermissionRationaleDialog(
 *                 title     = stringResource(R.string.camera_rationale_title),
 *                 body      = stringResource(R.string.camera_rationale_body),
 *                 onConfirm = cameraState.onConfirm,   // wired — no launcher needed
 *                 onDismiss = { /* navigate back or show fallback */ }
 *             )
 *         }
 *         is PermissionState.PermanentlyDenied -> {
 *             // Deep-link user to Settings → App → Permissions
 *             OpenSettingsPrompt()
 *         }
 *         is PermissionState.Denied -> {
 *             Button(onClick = requestCamera) {
 *                 Text(stringResource(R.string.allow_camera))
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param permission  Android manifest permission string, e.g. [android.Manifest.permission.CAMERA].
 * @param onResult    Optional callback invoked each time the permission state changes after a
 *                    system dialog result. Useful for analytics or ViewModel intent dispatch.
 * @return            [Pair] of (current [PermissionState], lambda that opens the system dialog).
 */
@Composable
fun rememberTravelMonkPermission(
    permission: String,
    onResult: (PermissionState) -> Unit = {}
): Pair<PermissionState, () -> Unit> {
    val activity = LocalActivity.current

    // Resolve the initial state from the OS synchronously — no dialog shown here.
    fun resolveCurrentState(): PermissionState {
        if (activity == null) return PermissionState.Denied
        val isGranted = ContextCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
        if (isGranted) return PermissionState.Granted
        // shouldShowRationale is true only after a previous denial where the user did NOT
        // tap "Don't ask again". We resolve PermanentlyDenied inside the launcher callback
        // where both isGranted=false and shouldShowRationale=false can be conclusively mapped.
        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        return if (showRationale) PermissionState.ShowRationale(onConfirm = {}) else PermissionState.Denied
    }

    var state by remember(permission) { mutableStateOf(resolveCurrentState()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val newState: PermissionState = when {
            isGranted -> PermissionState.Granted

            // shouldShowRationale=true here → another denial, not "Don't ask again"
            activity != null &&
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) ->
                PermissionState.ShowRationale(onConfirm = {})

            // shouldShowRationale=false + not granted → "Don't ask again" was selected,
            // OR some OEM ROMs that never show rationale. Treat conservatively.
            else -> PermissionState.PermanentlyDenied
        }
        state = newState
        onResult(newState)
    }

    // Patch ShowRationale.onConfirm to wire directly into the launcher after it is created,
    // so the caller only needs cameraState.onConfirm() — no external launcher reference needed.
    val resolvedState = if (state is PermissionState.ShowRationale) {
        PermissionState.ShowRationale(onConfirm = { launcher.launch(permission) })
    } else {
        state
    }

    return resolvedState to { launcher.launch(permission) }
}
