package com.travelmonk.core.permissions

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.travelmonk.core.design.system.theme.TravelMonkTheme

/**
 * Displays a rationale dialog explaining why a runtime permission is needed before the
 * system dialog is shown again.
 *
 * Show this when [PermissionState.ShowRationale] is the active state. After the user confirms,
 * [PermissionState.ShowRationale.onConfirm] (pre-wired by [rememberTravelMonkPermission]) will
 * re-trigger the system permission dialog automatically.
 *
 * All colours and typography come from [TravelMonkTheme] tokens — nothing is hardcoded.
 *
 * ## Usage
 *
 * ```kotlin
 * val (cameraState, requestCamera) = rememberTravelMonkPermission(Manifest.permission.CAMERA)
 *
 * when (cameraState) {
 *     is PermissionState.ShowRationale -> PermissionRationaleDialog(
 *         title     = stringResource(R.string.camera_rationale_title),
 *         body      = stringResource(R.string.camera_rationale_body),
 *         onConfirm = cameraState.onConfirm,   // pre-wired → re-triggers system dialog
 *         onDismiss = { /* navigate back or show fallback UI */ }
 *     )
 *     is PermissionState.Granted           -> QrScannerContent()
 *     is PermissionState.PermanentlyDenied -> OpenSettingsPrompt()
 *     is PermissionState.Denied            -> Button(onClick = requestCamera) { ... }
 * }
 * ```
 *
 * @param title     Short heading stating which permission is needed.
 *                  Example: `"Camera access needed"`
 * @param body      One or two sentences explaining why the app needs the permission.
 *                  Be specific and honest — vague rationales reduce grant rates.
 *                  Example: `"TravelMonk uses the camera to scan QR codes on your booking confirmation."`
 * @param onConfirm Called when the user taps **Allow**. Typically pass
 *                  [PermissionState.ShowRationale.onConfirm] directly — it re-triggers the
 *                  system dialog. The dialog is dismissed before invoking this lambda.
 * @param onDismiss Called when the user taps **Not now** or dismisses by tapping outside.
 */
@Composable
fun PermissionRationaleDialog(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = TravelMonkTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = body,
                style = TravelMonkTheme.typography.bodyMedium,
                color = TravelMonkTheme.colors.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Dismiss first so the system dialog opens on a clean back-stack.
                    onDismiss()
                    onConfirm()
                }
            ) {
                Text(
                    text = "Allow",
                    color = TravelMonkTheme.colors.primary,
                    style = TravelMonkTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Not now",
                    color = TravelMonkTheme.colors.onSurfaceVariant,
                    style = TravelMonkTheme.typography.labelLarge
                )
            }
        }
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PermissionRationaleDialogPreview() {
    TravelMonkTheme {
        PermissionRationaleDialog(
            title = "Camera access needed",
            body = "TravelMonk uses the camera to scan QR codes on your booking confirmation. " +
                    "Your camera is never used without your action.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
