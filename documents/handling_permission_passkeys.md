# Plan: PasskeyPromptScreen Fixes + core/permissions/ Module

## Context

After implementing Credential Manager + passkey transaction key wiring in `PasskeyPromptScreen.kt`,
an architectural review identified correctness issues in the screen and a gap in the project's
permission infrastructure. Future features (camera for QR, file picker for travel docs,
POST_NOTIFICATIONS) will each need runtime permissions — centralising this now prevents
per-feature duplication and inconsistent UX.

Two deliverables:
1. Fix correctness issues in `PasskeyPromptScreen.kt`
2. Create a new `core/permissions/` module with a reusable Compose permission API

---

## Part 1: PasskeyPromptScreen.kt Fixes

**File:** `feature/onboarding/src/main/java/com/travelmonk/feature/onboarding/ui/PasskeyPromptScreen.kt`

### Fix 1 — HIGH: Fragile Manual JSON Escaping in `buildSignedPayload`

Current hand-rolled `.replace()` chain misses Unicode escape sequences and control characters
(0x00–0x1F). Replace with `org.json.JSONObject` (built-in Android, zero new dependency):

```kotlin
private fun buildSignedPayload(credentialJson: String, txSig: String): String =
    JSONObject()
        .put("credential", credentialJson)
        .put("txKeyAlias", PASSKEY_TX_KEY_ALIAS)
        .put("txSig", txSig)
        .toString()
```

Import: `org.json.JSONObject`

### Fix 2 — MEDIUM: Magic Numbers for Biometric Error Codes

Replace literals `10` and `13` in `onAuthenticationError` with named constants:

```kotlin
if (errorCode == BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED ||
    errorCode == BiometricPrompt.BIOMETRIC_ERROR_NEGATIVE_BUTTON)
```

### Fix 3 — MEDIUM: Fully-Qualified Context Type in Private Functions

Private functions declare `context: android.content.Context` (fully qualified). Add
`import android.content.Context` at the file top and use unqualified `Context` in all four
private function signatures (`launchRegistration`, `launchAuthentication`, `signAndWrap`,
`performBiometricSigning`).

### Fix 4 — HIGH: Missing USE_BIOMETRIC Manifest Permission

`android.hardware.biometrics.BiometricPrompt.authenticate()` requires
`android.permission.USE_BIOMETRIC` to be declared in the app manifest. Without it the OS throws
a `SecurityException` at runtime the moment `performBiometricSigning` is called.

**Fix:** Add to `app/src/main/AndroidManifest.xml`:
```xml
<!-- Required by android.hardware.biometrics.BiometricPrompt (passkey transaction key signing) -->
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
```

This is a `normal` protection level permission — no runtime request dialog is needed, only the
manifest declaration. **Already applied.**

### Fix 5 — LOW: Document Concurrent Safety of LaunchedEffect

`LaunchedEffect(Unit)` collects `viewModel.effect` indefinitely. The ViewModel's `Channel`-based
effect delivery (RENDEZVOUS) guarantees exactly-once delivery — a second effect cannot be emitted
until the first is collected. Add a comment at the collection site documenting this contract.

---

## Part 2: core/permissions/ Module

### Motivation

Runtime permissions are needed across multiple upcoming features:
- Camera — QR scanning for booking reference lookup
- READ_MEDIA_IMAGES / file picker — travel document upload
- POST_NOTIFICATIONS — booking alerts (Android 13+)

Without a shared abstraction each feature re-implements launcher registration, rationale detection,
permanently-denied Settings deep-link, and rationale dialog UI independently.

### Module Structure

```
core/permissions/
├── build.gradle.kts
└── src/main/java/com/travelmonk/core/permissions/
    ├── PermissionState.kt                  — sealed interface
    ├── rememberTravelMonkPermission.kt     — @Composable returning state + request lambda
    └── PermissionRationaleDialog.kt        — @Composable dialog using TravelMonkTheme tokens
```

### PermissionState

```kotlin
sealed interface PermissionState {
    data object Granted : PermissionState
    data class ShowRationale(val onConfirm: () -> Unit) : PermissionState
    data object Denied : PermissionState
    data object PermanentlyDenied : PermissionState
}
```

### rememberTravelMonkPermission (API)

```kotlin
@Composable
fun rememberTravelMonkPermission(
    permission: String,
    onResult: (PermissionState) -> Unit = {}
): Pair<PermissionState, () -> Unit>
```

- Wraps `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`
- Uses `ContextCompat.checkSelfPermission` for current grant state
- Uses `ActivityCompat.shouldShowRequestPermissionRationale` (via `LocalActivity`) for rationale
- Returns `(currentState, requestLambda)` — caller controls when to trigger the request
- No Accompanist dependency — uses stable `ActivityResultContracts` API directly

### Usage example (future camera feature)

```kotlin
val (cameraState, requestCamera) = rememberTravelMonkPermission(Manifest.permission.CAMERA)

when (cameraState) {
    is PermissionState.Granted -> QrScannerContent(...)
    is PermissionState.ShowRationale -> PermissionRationaleDialog(
        title = stringResource(R.string.camera_rationale_title),
        body = stringResource(R.string.camera_rationale_body),
        onConfirm = cameraState.onConfirm,
        onDismiss = { /* dismiss */ }
    )
    is PermissionState.PermanentlyDenied -> OpenSettingsPrompt(...)
    is PermissionState.Denied -> Button(onClick = requestCamera) { Text("Allow Camera") }
}
```

### PermissionRationaleDialog

`AlertDialog` composable accepting `title: String`, `body: String`, `onConfirm: () -> Unit`,
`onDismiss: () -> Unit`. All colours/typography from `TravelMonkTheme` tokens — no hardcoding.
Must include Light + Dark `@Preview`.

### build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.library.compose)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)  // LocalActivity, rememberLauncherForActivityResult
    implementation(project(":core:design-system"))
}
```

### AndroidFeatureConventionPlugin Update

`build-logic/convention/src/main/kotlin/AndroidFeatureConventionPlugin.kt` — add alongside other
core deps:

```kotlin
add("implementation", project(":core:permissions"))
```

This makes `rememberTravelMonkPermission` and `PermissionRationaleDialog` available to every
feature module automatically, with no per-module boilerplate.

### settings.gradle.kts

Add `include(":core:permissions")` alongside existing core module includes.

---

## Files to Create / Modify

| Action | File |
|--------|------|
| Modify | `feature/onboarding/src/main/java/.../ui/PasskeyPromptScreen.kt` |
| Create | `core/permissions/build.gradle.kts` |
| Create | `core/permissions/src/main/java/com/travelmonk/core/permissions/PermissionState.kt` |
| Create | `core/permissions/src/main/java/com/travelmonk/core/permissions/rememberTravelMonkPermission.kt` |
| Create | `core/permissions/src/main/java/com/travelmonk/core/permissions/PermissionRationaleDialog.kt` |
| Modify | `settings.gradle.kts` — add `:core:permissions` include |
| Modify | `build-logic/convention/src/main/kotlin/AndroidFeatureConventionPlugin.kt` — add dep |

---

## Verification

```bash
# Part 1 — onboarding module compiles cleanly
./gradlew :feature:onboarding:compileDebugKotlin

# Part 2 — permissions module compiles standalone
./gradlew :core:permissions:compileDebugKotlin

# Full build green
./gradlew assembleDebug
```