# Security Hardening — Session Key & DataStore
Last updated: 2026-05-18

This document covers the production security hardening of the TravelMonk session DataStore,
specifically the Android Keystore master key policy, key rotation, and backup exclusion.
It is the reference for the items originally deferred in `tasks/onborarding_module_review.md`.

---

## Threat Model

| Threat | Mitigation |
|--------|-----------|
| Offline extraction — attacker dumps flash, reads encrypted files on another machine | Hardware-backed key never leaves TEE/StrongBox; extracted ciphertext is useless |
| Locked device in attacker's hand | `setUnlockedDeviceRequired(true)` — key unusable while screen is locked |
| Stolen device + attacker enrolls new biometric | `setInvalidatedByBiometricEnrollment(true)` — key permanently invalidated on new enrollment |
| Long-lived token compromise | 30-day key rotation wipes session; user re-authenticates |
| Backup replay attack | Session files and keysets excluded from Auto Backup, Cloud Backup, and Device Transfer |

---

## Architectural Decision — Item a: `setUserAuthenticationRequired` on the DataStore Key

**Decision: NOT applied to the DataStore master key. Applied only to the future passkey transaction key.**

### Why `setUserAuthenticationRequired(true)` + `validity=0` is wrong here

`validity=0` means **per-operation biometric** — the OS requires a completed `BiometricPrompt`
before every cryptographic call. The DataStore master key is used:

1. At cold start — Tink decrypts its own keyset using the master key
2. On every `sessionFlow.collect {}` — DataStore reads the file when observers activate
3. On every `saveSession()` / `markOnboardingComplete()` write

With `validity=0`, each of those operations triggers `UserNotAuthenticatedException` unless a
`BiometricPrompt.authenticate()` success happened in the immediate call chain. At cold start,
before `MainActivity.onCreate()` finishes, the Hilt graph tries to `build()` the
`AndroidKeysetManager` and the OS throws — **Hilt graph construction fails, app crashes at launch.**

### Why `validity=N` (time-window) doesn't work on StrongBox

StrongBox (physically separate chip — e.g. Titan M) has no concept of "time since last auth"
because it cannot trust system time provided by the application processor. StrongBox only accepts
`validity=0`. If we set `validity=30` in `buildKeySpec()`, `generateNewKeyWithSpec()` throws
`IllegalArgumentException` on StrongBox devices. Our StrongBox → TEE fallback would catch it, but
then StrongBox devices silently lose the user-auth binding — defeating the purpose.

### The right substitute for the DataStore key

`setUnlockedDeviceRequired(true)` — key is unusable while device is locked (screen off / PIN
screen showing). This defeats offline extraction and background access while device is at rest,
without requiring a BiometricPrompt on cold start.

### Where `setUserAuthenticationRequired(true)` + `validity=0` DOES belong

A **passkey transaction key** — used only when the user explicitly triggers passkey sign-in or
registration and a `BiometricPrompt` is already showing. This key will be introduced when
passkey backend integration lands in `PasskeyPromptViewModel`.

```
DataStore master key
  setUnlockedDeviceRequired(true)              ← correct for a storage encryption key
  setInvalidatedByBiometricEnrollment(true)    ← defence-in-depth

Passkey transaction key (future)
  setUserAuthenticationRequired(true)
  setUserAuthenticationValidityDurationSeconds(0)   ← per-op biometric, correct here
  setIsStrongBoxBacked(true) with TEE fallback
```

---

## Implementation: `buildKeySpec()` additions (items b + c)

File: `feature/onboarding/src/main/java/com/travelmonk/feature/onboarding/di/SessionModule.kt`

```kotlin
private fun buildKeySpec(alias: String, useStrongBox: Boolean): KeyGenParameterSpec =
    KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .setUnlockedDeviceRequired(true)                 // item b — API 28+, minSdk=28
        .setInvalidatedByBiometricEnrollment(true)       // item c
        .apply { if (useStrongBox) setIsStrongBoxBacked(true) }
        .build()
```

### Error recovery added to `provideSessionAead()`

**`KeyPermanentlyInvalidatedException`** — biometric enrollment changed; key gone forever.

```
1. AndroidKeystore.deleteKey(alias)
2. Clear Tink keyset SharedPreferences
3. Delete DataStore file
4. generateMasterKey() — fresh key
5. Rebuild AndroidKeysetManager — fresh keyset
→ Session is empty; user re-authenticates at auth gate
```

**`UserNotAuthenticatedException`** — device was locked when the key was accessed.

```
→ Throw DeviceLockedSessionException (typed, extends SecurityException)
→ App cold start: authStateFlow defaults to EMPTY, no crash
→ Background callers must catch and defer
```

---

## Implementation: Key Rotation (item d)

### Policy

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| Rotation interval | 30 days | Auth tokens warrant more aggressive rotation than a 90-day default |
| Rotation trigger | Cold start check | No background job needed; check is an O(1) SharedPreferences read |
| User impact | Session cleared silently | User re-authenticates via phone+OTP — same as first install |

### `KeyRotationManager` (private object inside `SessionModule.kt`)

```kotlin
private const val KEY_META_PREF_FILE   = "travelmonk_key_meta"
private const val KEY_META_CREATED_AT  = "master_key_created_at"
private const val ROTATION_INTERVAL_MS = 30L * 24 * 3600 * 1000   // 30 days

private object KeyRotationManager {
    fun shouldRotate(context: Context): Boolean {
        val createdAt = prefs(context).getLong(KEY_META_CREATED_AT, 0L)
        return createdAt > 0L &&
               System.currentTimeMillis() - createdAt > ROTATION_INTERVAL_MS
    }
    fun recordKeyCreation(context: Context) {
        prefs(context).edit().putLong(KEY_META_CREATED_AT, System.currentTimeMillis()).apply()
    }
    fun clearRecord(context: Context) {
        prefs(context).edit().remove(KEY_META_CREATED_AT).apply()
    }
    private fun prefs(context: Context) =
        context.getSharedPreferences(KEY_META_PREF_FILE, Context.MODE_PRIVATE)
}
```

### Rotation procedure

```kotlin
private fun performKeyRotation(context: Context, alias: String, supportsStrongBox: Boolean) {
    TravelMonkLogger.i(TAG, "Master key rotation triggered (>30 days)")
    context.dataStoreFile(SESSION_FILE).delete()                         // wipe encrypted data
    context.getSharedPreferences(KEYSET_PREF_FILE, Context.MODE_PRIVATE)
           .edit().clear().apply()                                        // wipe Tink keyset
    if (AndroidKeystore.hasKey(alias)) AndroidKeystore.deleteKey(alias)  // delete old key
    KeyRotationManager.clearRecord(context)
    generateMasterKey(alias, supportsStrongBox)
    KeyRotationManager.recordKeyCreation(context)
    TravelMonkLogger.i(TAG, "Key rotation complete — user must re-authenticate")
}
```

### Cold-start call sequence in `provideSessionAead()`

```
AeadConfig.register()
if (!hasKey) → generateMasterKey() + recordKeyCreation()
if (shouldRotate()) → performKeyRotation()
verifyHardwareBacking()
AndroidKeysetManager.Builder().build()
  catch KeyPermanentlyInvalidatedException → wipe + regenerate
  catch UserNotAuthenticatedException      → throw DeviceLockedSessionException
return AEAD primitive
```

---

## Implementation: Backup Exclusion (item e)

### Why this matters

Android Keystore keys are hardware-bound — they cannot be exported to another device. If the
DataStore file or Tink keyset SharedPreferences are backed up without the key, the restore is
useless (undecryptable). However, the presence of these files in a backup is still a data leak
surface: an attacker with access to the backup can attempt offline attacks against the encryption,
or attempt to use the keyset on a rooted device with a forged key import.

Excluding these files costs nothing and closes the leak surface.

### `app/src/main/res/xml/data_extraction_rules.xml` — API 31+ (Cloud Backup + Device Transfer)

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="sharedpref" path="travelmonk_tink_prefs.xml"/>
        <exclude domain="sharedpref" path="travelmonk_key_meta.xml"/>
        <exclude domain="file"       path="datastore/travelmonk_session.db"/>
    </cloud-backup>
    <device-transfer>
        <exclude domain="sharedpref" path="travelmonk_tink_prefs.xml"/>
        <exclude domain="sharedpref" path="travelmonk_key_meta.xml"/>
        <exclude domain="file"       path="datastore/travelmonk_session.db"/>
    </device-transfer>
</data-extraction-rules>
```

### `app/src/main/res/xml/backup_rules.xml` — API 23–30 (Full Auto Backup)

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="sharedpref" path="travelmonk_tink_prefs.xml"/>
    <exclude domain="sharedpref" path="travelmonk_key_meta.xml"/>
    <exclude domain="file"       path="datastore/travelmonk_session.db"/>
</full-backup-content>
```

Both files are already referenced in `app/src/main/AndroidManifest.xml` via
`android:dataExtractionRules` and `android:fullBackupContent` — no manifest change needed.

---

---

## Completion Status

Last updated: 2026-05-18

| Item | Description | Status | File(s) Changed |
|------|-------------|--------|-----------------|
| a | `setUserAuthenticationRequired(true)` + `validity=0` on DataStore key | DEFERRED — architectural decision (see above) | — |
| b | `setUnlockedDeviceRequired(true)` — key unusable while device locked | DONE | `SessionModule.kt` — `buildKeySpec()` |
| c | `setInvalidatedByBiometricEnrollment(true)` + `KeyPermanentlyInvalidatedException` recovery | DONE | `SessionModule.kt` — `buildKeySpec()`, `buildKeysetManager()` |
| d | 30-day key rotation via `KeyRotationManager` + `performKeyRotation()` | DONE | `SessionModule.kt` — `KeyRotationManager`, `performKeyRotation()`, `provideSessionAead()` |
| e | Backup exclusion for DataStore file + Tink keyset + key metadata | DONE | `data_extraction_rules.xml`, `backup_rules.xml` |

### New symbols introduced (all in `SessionModule.kt`)

| Symbol | Type | Purpose |
|--------|------|---------|
| `DeviceLockedSessionException` | `class` | Thrown when `setUnlockedDeviceRequired` blocks key access (device locked) |
| `KeyRotationManager` | `private object` | Tracks key creation epoch; drives 30-day rotation check |
| `performKeyRotation()` | `private fun` | Wipes session + keyset + key, regenerates on 30-day trigger |
| `buildKeysetManager()` | `private fun` | Wraps `AndroidKeysetManager.Builder().build()` with error recovery |
| `KEY_META_PREF_FILE` | `const` | SharedPreferences file name for key metadata (`travelmonk_key_meta`) |
| `KEY_META_CREATED_AT` | `const` | Key name for creation timestamp in key metadata prefs |
| `ROTATION_INTERVAL_MS` | `const` | 30-day interval in milliseconds |

---

## Verification Checklist

- [ ] `./gradlew :feature:onboarding:compileDebugKotlin` — clean compile
- [ ] `./gradlew :feature:onboarding:testDebugUnitTest` — all 34 existing tests pass
- [ ] Emulator smoke: fresh install → logcat shows "Generated TEE-backed master key" + "Session master key is hardware-backed"
- [ ] Rotation smoke: set `ROTATION_INTERVAL_MS = 1L` temporarily → cold start → logcat shows "Key rotation complete" → DataStore empty → auth gate shown → restore constant
- [ ] Backup smoke: `adb backup -apk -f backup.ab com.travelmonk` → unpack → confirm `travelmonk_session.db`, `travelmonk_tink_prefs.xml`, `travelmonk_key_meta.xml` absent

---

## Future Work

- **Passkey transaction key** — introduce a second key with `setUserAuthenticationRequired(true)` + `validity=0`, used only inside the BiometricPrompt callback in `PasskeyPromptViewModel`.
- **Rotation UX** — show a "security refresh required" screen after rotation instead of silently dropping the user at the welcome screen (deferred until UX design is finalised).
- **Remote rotation interval** — drive `ROTATION_INTERVAL_MS` from a feature flag so the security team can tighten the window without a release.

-  Nice to have (Low)
  Add rotation UX — show "security refresh" screen instead of silent session clear on 30-day rotation 
  Drive rotation interval from feature flag — allows security team to tighten without release

  ┌────────────┬───────────────────────────────┬───────────────────────────────────────────┬──────────────────────────┬───────────────────┬────────────────────────┐
  │  Company   │              OTP              │              Session Storage              │         Passkey          │   Key Rotation    │    Hardware Verify     │
  ├────────────┼───────────────────────────────┼───────────────────────────────────────────┼──────────────────────────┼───────────────────┼────────────────────────┤
  │ Google Pay │ Phone + E.164 validation      │ EncryptedSharedPrefs / DataStore +        │ FIDO2 Credential Manager │ Per-token expiry  │ Yes, StrongBox         │
  │            │                               │ AES-GCM                                   │                          │                   │ required               │
  ├────────────┼───────────────────────────────┼───────────────────────────────────────────┼──────────────────────────┼───────────────────┼────────────────────────┤
  │ Uber       │ Rate-limited (3 resends max)  │ Encrypted DataStore                       │ No passkey (PIN          │ Token refresh     │ TEE                    │
  │            │                               │                                           │ fallback)                │ only              │                        │
  ├────────────┼───────────────────────────────┼───────────────────────────────────────────┼──────────────────────────┼───────────────────┼────────────────────────┤
  │ Paytm      │ Rate-limited + OTP countdown  │ EncryptedSharedPrefs                      │ No                       │ None              │ Not verified           │
  ├────────────┼───────────────────────────────┼───────────────────────────────────────────┼──────────────────────────┼───────────────────┼────────────────────────┤
  │ WhatsApp   │ Rate-limited via backend      │ Signal Protocol + local keys              │ No                       │ No                │ Yes                    │
  ├────────────┼───────────────────────────────┼───────────────────────────────────────────┼──────────────────────────┼───────────────────┼────────────────────────┤
  │ TravelMonk │ E.164 validated, no rate      │ Tink AEAD + Keystore                      │ FIDO2 stubbed            │ 30-day rotation   │ Yes, cold-start        │
  │            │ limit                         │                                           │                          │                   │                        │
  └────────────┴───────────────────────────────┴───────────────────────────────────────────┴──────────────────────────┴───────────────────┴──────────────────────