Onboarding Module — Principal Architect Review (Consolidated)
Last updated: 2026-05-18 | Reviewed across 3 passes | 24 of 24 issues resolved

---
WHAT IS PRODUCTION-QUALITY (unchanged foundations)

- MVI pattern — correctly implemented everywhere. State/Intent/Effect, BaseViewModel,
  collectAsStateWithLifecycle(), stateless *Content composables, Light + Dark previews.
- Encrypted storage — Tink AES256-GCM + Android Keystore. StrongBox requested with TEE fallback,
  hardware-backing verified on every cold start, emulator bypass for CI/dev.
- Navigation architecture — OnboardingNavigationBus + typed OnboardingNavKey. Onboarding runs in
  its own nav host, fully decoupled from the tab-based NavigationBus.
- DTO + Mapper pattern — AuthResponseDto → toToken() → domain model. Data/domain wall is solid.
- Fake data pattern — real API call commented immediately above each fake, clear TODOs.
- DI — Hilt throughout, @Singleton correct, @Binds + @Provides properly separated.
- Repository interfaces — domain layer is clean and independent of the data layer.

---
ISSUE INDEX — 24 total | 24 FIXED | 0 PENDING

+-----+----------+--------+-------------------------------------------------------+------------------------------------------+
|  #  | Severity | Status |                         Issue                         |                   File                   |
+-----+----------+--------+-------------------------------------------------------+------------------------------------------+
|  1  | CRITICAL | FIXED  | Dead nav effects — screens never navigated            | PhoneEntryViewModel, OtpViewModel        |
|  2  | CRITICAL | FIXED  | Race condition — PasskeyPrompt torn down before shown | SessionData, UserSessionStore            |
|  3  | CRITICAL | FIXED  | userId never persisted — isAuthenticated always false | AuthMapper, AuthToken, UserSessionStore  |
|  4  | CRITICAL | FIXED  | StrongBox unconditional crash on first launch         | SessionModule                            |
|  5  | HIGH     | FIXED  | WelcomeEffect.NavigateToPhoneEntry is dead code       | WelcomeViewModel, WelcomeScreen          |
|  6  | HIGH     | FIXED  | resendOtp() swallows failures silently                | OtpViewModel                             |
|  7  | HIGH     | FIXED  | No phone number format validation                     | PhoneEntryViewModel                      |
|  8  | HIGH     | FIXED  | beginRegistration() passes empty userId               | PasskeyPromptViewModel                   |
|  9  | HIGH     | FIXED  | PasskeyPromptEffect.ShowError/NavigateToHome dead     | PasskeyPromptMvi, PasskeyPromptViewModel |
| 10  | HIGH     | FIXED  | Duplicate markPasskeyRegistered() double-write        | PasskeyPromptViewModel                   |
| 11  | HIGH     | FIXED  | Dead ShowError effects in PhoneEntry + Otp MVI        | PhoneEntryMvi, OtpMvi, screens           |
| 12  | HIGH     | FIXED  | Fake passkey auth/registration returns blank userId   | PasskeyRepositoryImpl                    |
| 13  | MEDIUM   | FIXED  | Hardcoded UI strings in all four screens              | All 4 screens + strings.xml              |
| 14  | MEDIUM   | FIXED  | MaterialTheme.* used instead of TravelMonkTheme.*     | All 4 screens                            |
| 15  | MEDIUM   | FIXED  | Resend countdown lost on configuration change         | OtpViewModel                             |
| 16  | MEDIUM   | FIXED  | @Suppress("UNUSED_VARIABLE") noise in PasskeyPrompt  | PasskeyPromptScreen                      |
| 17  | MEDIUM   | FIXED  | CircularProgressIndicator height instead of size      | PhoneEntryScreen, OtpScreen              |
| 18  | MEDIUM   | FIXED  | OTP digits-only not enforced in ViewModel             | OtpViewModel                             |
| 19  | MEDIUM   | FIXED  | WelcomeState.isLoading is dead state                  | WelcomeMvi, WelcomeScreen                |
| 20  | MEDIUM   | FIXED  | ViewModel error fallbacks hardcoded (need UiText)     | PhoneEntryVM, OtpVM, PasskeyPromptVM     |
| 21  | MEDIUM   | FIXED  | Zero unit tests despite test dependencies declared    | All test source sets                     |
| 22  | HIGH     | FIXED  | StrongBoxUnavailableException uncaught — app crash    | SessionModule                            |
| 23  | HIGH     | FIXED  | Hardware-backing not verified for pre-existing keys   | SessionModule                            |
| 24  | HIGH     | FIXED  | Software-backed key silently accepted on real device  | SessionModule                            |
+-----+----------+--------+-------------------------------------------------------+------------------------------------------+

---
DETAILED FINDINGS

---
#1 — CRITICAL | FIXED
Dead navigation effects — screens never navigated

Issue:
PhoneEntryViewModel emitted PhoneEntryEffect.NavigateToOtp and OtpViewModel emitted
OtpEffect.NavigateToPasskeyPrompt, but neither screen had a LaunchedEffect to collect effects.
The effect channel fired, nothing listened, and the user was permanently stuck.

Fix applied:
WelcomeViewModel now emits WelcomeEffect.NavigateToPhoneEntry instead of calling the navigator
directly. All three screens (WelcomeScreen, PhoneEntryScreen, OtpScreen) receive OnboardingNavigator
as a composable parameter and collect effects in a LaunchedEffect(Unit) block. OnboardingFlow and
MainActivity were updated to wire the navigator through.

---
#2 — CRITICAL | FIXED
Race condition — PasskeyPrompt is torn down before it is shown

Issue:
VerifyOtpUseCase called userSessionStore.saveSession() immediately on OTP success. That DataStore
write triggered authStateFlow → Authenticated, which caused MainActivity to tear down the entire
onboarding back stack before OtpEffect.NavigateToPasskeyPrompt had a chance to push the screen.

Fix applied:
Added onboardingComplete: Boolean = false to SessionData. isAuthenticated now requires both
accessToken.isNotBlank() && userId.isNotBlank() && onboardingComplete. saveSession() leaves
onboardingComplete untouched (defaults false). PasskeyPromptViewModel calls markOnboardingComplete()
only after the user acts (Skip / Register success / Auth success). Survives process death because
the flag is persisted in the encrypted DataStore.

---
#3 — CRITICAL | FIXED
isAuthenticated always false — userId is never persisted

Issue:
AuthResponseDto had userId and phone fields. AuthMapper.toToken() silently dropped both.
UserSessionStore.saveSession() defaulted userId = "". SessionData.isAuthenticated requires
userId.isNotBlank(), so the auth gate never opened after a successful OTP.

Fix applied:
AuthToken extended with userId and phoneNumber fields. AuthMapper.toToken() now maps all four
fields. UserSessionStore.saveSession() reads from the token with .ifBlank { current.userId }
fallback to preserve existing values. AuthRepositoryImpl fake data updated with realistic mock
userId ("usr_fake_001") and phoneNumber ("+919876543210").

---
#4 — CRITICAL | FIXED
StrongBox unconditional crash on first launch

Issue:
SessionModule.provideSessionAead() called setIsStrongBoxBacked(true) unconditionally inside the
if (!AndroidKeystore.hasKey(masterKeyAlias)) block. The vast majority of Android devices lack a
dedicated StrongBox security chip. On these devices, generateNewKeyWithSpec() throws
StrongBoxUnavailableException on first launch, crashing the app before the DataStore or Tink keyset
is created. The code comment said "API 33+ StrongBox" but there was no SDK_INT check and no
exception handler.

Fix applied:
setIsStrongBoxBacked(true) is now wrapped in try/catch StrongBoxUnavailableException. On
StrongBoxUnavailableException the key is regenerated without the StrongBox flag, falling back to a
standard hardware-backed TEE key. minSdk is 28 so no API-level guard is needed.

---
#5 — HIGH | FIXED
WelcomeEffect.NavigateToPhoneEntry is dead code

Issue:
WelcomeViewModel called navigator.toPhoneEntry() directly (bypassing MVI). WelcomeEffect existed
but was never emitted. The screen had no LaunchedEffect to collect it.

Fix applied:
WelcomeViewModel now emits WelcomeEffect.NavigateToPhoneEntry. WelcomeScreen collects it and calls
navigator.toPhoneEntry(). Consistent with the MVI UDF pattern used by the other screens.

---
#6 — HIGH | FIXED
resendOtp() silently swallows errors

Issue:
OtpViewModel.resendOtp() called sendOtpUseCase(phone) and discarded the result entirely. The 30s
cooldown timer started regardless of success or failure. If the resend failed, the user saw a
countdown but received no code and no error explanation.

Fix applied:
resendOtp() now observes the result. On DataResult.Error the cooldown is not started and
state.error is set. On DataResult.Success the cooldown starts and the SavedStateHandle is updated.

---
#7 — HIGH | FIXED
No phone number format validation

Issue:
PhoneEntryScreen enabled the submit button on state.phone.isNotBlank(). Submitting "a" or "1"
would reach sendOtpUseCase with no validation. No E.164 format check, no country code guard.

Fix applied:
PhoneEntryViewModel.sendOtp() validates E.164 format before calling the use case:
must start with "+", digit count must be 7–15 (ITU-T standard). Validation error is surfaced
via state.error; the use case is not called on invalid input.

---
#8 — HIGH | FIXED
PasskeyPromptViewModel.beginRegistration() passes empty userId

Issue:
beginRegistration() called passkeyRepository.beginRegistration(userId = ""). The server needs the
authenticated user's ID to associate the new passkey credential with the account.

Fix applied:
PasskeyPromptViewModel injects UserSessionStore and reads sessionFlow.value.userId in both
beginRegistration() and completeRegistration(). The userId set by VerifyOtpUseCase via saveSession()
is now correctly forwarded to the passkey registration ceremony.

---
#9 — HIGH | FIXED
PasskeyPromptEffect.ShowError and NavigateToHome were dead

Issue:
PasskeyPromptViewModel set state { copy(error = ...) } on failures but never emitted ShowError.
NavigateToHome was redundant after the DataStore auth gate fix (#2) was applied.

Fix applied:
Both NavigateToHome and ShowError removed from PasskeyPromptEffect. Errors surface via state.error
(already observed by PasskeyPromptContent). Auth gate opens via markOnboardingComplete() in the
DataStore, observed by MainActivity.authStateFlow.

---
#10 — HIGH | FIXED
Duplicate markPasskeyRegistered() double-write

Issue:
PasskeyRegistrationUseCase.invoke() already called userSessionStore.markPasskeyRegistered().
PasskeyPromptViewModel.completeRegistration() also called it, causing two DataStore writes for
a single registration event and violating single-responsibility between use case and ViewModel.

Fix applied:
Removed markPasskeyRegistered() from PasskeyPromptViewModel.completeRegistration(). The use case
owns the passkeyRegistered flag. The ViewModel only calls markOnboardingComplete() to open the
auth gate.

---
#11 — HIGH | FIXED
Dead ShowError effects in PhoneEntry and Otp MVI

Issue:
PhoneEntryEffect.ShowError and OtpEffect.ShowError were defined in their sealed interfaces but
neither ViewModel ever emitted them. Errors in both flows already surface via state.error. The
dead variants and their empty when-branches in the screens added confusion.

Fix applied:
ShowError removed from PhoneEntryEffect and OtpEffect. The corresponding dead when-branches
removed from PhoneEntryScreen and OtpScreen.

---
#12 — HIGH | FIXED
Fake passkey auth/registration returns blank userId — isAuthenticated never true

Issue:
PasskeyRepositoryImpl.completeAuthentication() and completeRegistration() returned
AuthToken("fake_passkey_access_token", "fake_passkey_refresh_token") with blank userId and
phoneNumber. For a returning user signing in via passkey only (no prior OTP session), there is
no existing userId in the store to preserve via .ifBlank. isAuthenticated requires
userId.isNotBlank(), so the auth gate never opened for the passkey-only sign-in path.

Fix applied:
Both fake AuthToken returns now include userId = "usr_fake_001" and phoneNumber = "+919876543210".

---
#13 — MEDIUM | FIXED
Hardcoded UI strings in all four screens

Issue:
"TravelMonk", "Your journey begins here", "Enter your phone number", "Send Code", "Verify",
"Resend in %ds", etc. were inline string literals. CLAUDE.md explicitly requires
stringResource(R.string.*). Hardcoded strings block localisation.

Fix applied:
All UI strings extracted to feature/onboarding/src/main/res/values/strings.xml.
All four screens now use stringResource(R.string.*) exclusively.

---
#14 — MEDIUM | FIXED
MaterialTheme.* used instead of TravelMonkTheme.*

Issue:
All four screens used MaterialTheme.typography.*, MaterialTheme.colorScheme.*, and raw 24.dp
spacing literals. The design system rules (.claude/rules/design-system.md) prohibit this.

Fix applied:
All four screens updated to use TravelMonkTheme.typography.*, TravelMonkTheme.colors.*,
and TravelMonkTheme.spacing.* tokens throughout.

---
#15 — MEDIUM | FIXED
Resend countdown lost on configuration change

Issue:
OtpViewModel received SavedStateHandle but only used it to read the phone number. The countdown
timer state (resendCooldownSeconds) was held only in memory — lost on rotation or process death.
The user got a fresh 30s timer on every rotation mid-countdown.

Fix applied:
OtpViewModel now reads the initial cooldown from savedStateHandle.get<Int>(KEY_RESEND_COOLDOWN)
and resumes the countdown on init if a value is present. Every tick writes the remaining seconds
back to SavedStateHandle so it survives both config changes and process death.

---
#16 — MEDIUM | FIXED
@Suppress("UNUSED_VARIABLE") noise in PasskeyPromptScreen

Issue:
val context = LocalContext.current was kept under @Suppress("UNUSED_VARIABLE") because it was
only referenced in commented-out credential manager code.

Fix applied:
@Suppress annotation and val context removed. The TODO comments for the Credential Manager
integration remain intact — context will be captured inside the LaunchedEffect branches when
backend integration lands.

---
#17 — MEDIUM | FIXED
CircularProgressIndicator sized with height instead of size

Issue:
CircularProgressIndicator(modifier = Modifier.height(20.dp)) in PhoneEntryScreen and OtpScreen.
Height-only constraint clips the indicator rather than resizing it — produces a cropped oval.

Fix applied:
Changed to Modifier.size(20.dp) in PhoneEntryScreen, OtpScreen, and PasskeyPromptScreen.

---
#18 — MEDIUM | FIXED
OTP digits-only not enforced in ViewModel

Issue:
OtpScreen used KeyboardType.NumberPassword which is a soft keyboard hint, not a guarantee.
OtpViewModel.handleIntent(OtpChanged) accepted any string with length <= 6. Pasting "abc123"
would pass through.

Fix applied:
OtpViewModel checks intent.otp.all { it.isDigit() } before applying the state update. Non-digit
input is silently discarded, consistent with how the field is presented to the user.

---
#19 — MEDIUM | FIXED
WelcomeState.isLoading is dead state

Issue:
WelcomeState had isLoading: Boolean = false that WelcomeViewModel never set to true. WelcomeContent
used it in enabled = !state.isLoading for both buttons, but the guard was permanently true —
the buttons could never be disabled.

Fix applied:
isLoading removed from WelcomeState. The enabled parameter removed from both Button and
OutlinedButton in WelcomeContent, making the actual behaviour match the intended behaviour.

---
#20 — MEDIUM | FIXED
ViewModel error fallback strings hardcoded — UiText pattern needed

Issue:
ViewModels store error messages as raw String in state but cannot call context.getString()
without injecting Context (an anti-pattern). Hardcoded fallbacks existed in PhoneEntryViewModel,
OtpViewModel, and PasskeyPromptViewModel, blocking localisation.

Fix applied:
1. UiText sealed class introduced in core/common/src/main/java/com/travelmonk/core/common/ui/UiText.kt:
     sealed class UiText {
         @Immutable data class Raw(val value: String) : UiText()
         @Immutable data class Res(@StringRes val id: Int) : UiText()
         fun asString(context: Context): String
     }
2. state.error changed from String? to UiText? in PhoneEntryState, OtpState, PasskeyPromptState.
3. ViewModels updated — known errors emit UiText.Res(R.string.onboarding_error_*),
   dynamic API messages emit UiText.Raw(result.message ?: fallback):
     PhoneEntryViewModel : UiText.Res(onboarding_error_invalid_phone), UiText.Res(onboarding_error_send_otp_failed)
     OtpViewModel        : UiText.Res(onboarding_error_otp_verification_failed), UiText.Res(onboarding_error_resend_failed)
     PasskeyPromptViewModel: UiText.Res for all 4 passkey error strings
4. All four screens resolve via state.error?.asString(LocalContext.current) at the composable layer.
5. All 8 error string keys present in strings.xml.

---
#21 — MEDIUM | FIXED
Zero unit tests despite test dependencies being declared

Issue:
feature/onboarding/build.gradle.kts declared junit4, turbine, mockk, and
kotlinx.coroutines.test but no test files existed. Per CLAUDE.md the coverage target is 80%.

Fix applied — 4 test files added covering 34 test cases total:

PhoneEntryViewModelTest (9 tests)
  - Validation: blank phone, no-plus prefix, too-few digits, too-many digits → error set
  - phoneChanged() clears existing error
  - Success: NavigateToOtp effect emitted, loading and error cleared
  - Error: UiText.Raw with message, UiText.Res fallback without message

OtpViewModelTest (11 tests)
  - Digits-only guard: non-digit input discarded, digit-only accepted
  - Verify: success emits NavigateToPasskeyPrompt; error with message → Raw; error without → Res
  - Resend: success starts cooldown; error sets error + no cooldown; second resend during cooldown ignored
  - SavedStateHandle: cooldown initialised from saved state; ticks down to zero
  - OTP change clears existing error

VerifyOtpUseCaseTest (6 tests)
  - Success: saveSession() called with token; featureFlagSyncer.sync() called; token returned unchanged
  - Error: saveSession() not called; sync not called; DataResult.Error returned unchanged

SessionDataSerializerTest (8 tests)
  - Round-trip: write-then-read returns original SessionData; isAuthenticated true after round-trip
  - Empty input: returns SessionData.EMPTY; does not throw
  - Tampered ciphertext: returns SessionData.EMPTY; does not throw
  - defaultValue is SessionData.EMPTY; SessionData.EMPTY.isAuthenticated is false

---
#22 — HIGH | FIXED
StrongBoxUnavailableException uncaught on generateNewKeyWithSpec() — crash on first launch

Issue:
SessionModule.provideSessionAead() set setIsStrongBoxBacked(true) via buildKeySpec() but wrapped
only the key-generation block in the outer if (!hasKey) guard. The StrongBox path had no catch
for StrongBoxUnavailableException. Even with setIsStrongBoxBacked(true), the OS throws
StrongBoxUnavailableException at runtime whenever the chip is absent or busy — even on devices
that advertise FEATURE_STRONGBOX_KEYSTORE. The app crashed before the DataStore or Tink keyset
could be created.

Fix applied:
generateMasterKey() helper introduced. It first attempts generateNewKeyWithSpec(strongBoxSpec).
On StrongBoxUnavailableException it retries immediately with generateNewKeyWithSpec(teeSpec) —
a TEE-backed key without the StrongBox flag. The fallback is logged at WARN level. No API-level
guard required because minSdk is 28 and both StrongBox and TEE paths are available from API 23.

---
#23 — HIGH | FIXED
Hardware-backing not verified for keys that already exist — silent downgrade after key migration

Issue:
verifyHardwareBacking() was called only inside the if (!AndroidKeystore.hasKey(masterKeyAlias))
block. If a pre-existing software-backed key was already in the Keystore from a previous build
(e.g., an emulator key loaded onto a real device via backup, or an early debug build before
StrongBox was required), the verification was skipped entirely on every subsequent cold start.
A compromised or software-backed key would silently persist in production.

Fix applied:
verifyHardwareBacking() moved outside the if (!hasKey) block — it now runs on every cold start
regardless of whether the key was just created or already existed. If the key fails hardware
verification on a real device, HardwareSecurityException is thrown and propagated to the DI
graph, forcing the app into a safe error state rather than operating with a weak key.

---
#24 — HIGH | FIXED
Software-backed key silently accepted on real device — no enforcement

Issue:
The old code called setIsStrongBoxBacked(true) as a hint but never checked the resulting key's
actual security level. On devices where StrongBox is unavailable and the OS silently falls back
to a software key (security level = SOFTWARE), the DataStore was encrypted with a key that an
attacker with root access could extract. Production apps must reject software-backed keys.

Fix applied:
verifyHardwareBacking() uses KeyFactory + KeyInfo to read securityLevel (API 31+) or the
legacy isInsideSecureHardware (API < 31). If the resolved level is SOFTWARE and the device is
not an emulator, HardwareSecurityException is thrown:
  "Master key is software-backed on a real device — refusing to continue"
isEmulator() checks Build.FINGERPRINT, Build.MODEL, and Build.MANUFACTURER so that debug and
CI builds on emulators continue to work without hardware-backing. The custom
HardwareSecurityException (extends SecurityException) is defined in the same file and can be
caught by a top-level Hilt error handler for a user-visible error screen in a future pass.

---
ARCH-DECISION — Item a: setUserAuthenticationRequired not applied to the DataStore key

setUserAuthenticationRequired(true) + setUserAuthenticationValidityDurationSeconds(0) means
per-operation biometric — the OS requires a completed BiometricPrompt before every crypto call.
The DataStore master key is used at cold start (Tink keyset decryption), on every sessionFlow
collect, and on every saveSession/markOnboardingComplete write. Applying validity=0 here
requires a BiometricPrompt dialog before Hilt graph construction completes — crash at launch.

validity=N (time-window) is rejected by StrongBox — it only accepts validity=0. Setting
validity=30 throws IllegalArgumentException on StrongBox devices, silently losing the
user-auth binding on the StrongBox path.

Decision: setUnlockedDeviceRequired(true) (item b) is the correct policy for the DataStore key.
setUserAuthenticationRequired(true) + validity=0 is reserved for the passkey transaction key,
introduced when passkey backend integration lands in PasskeyPromptViewModel.

Full rationale: documents/security_hardening.md — "Architectural Decision — Item a"

---
SECURITY HARDENING — Pass 4 | 2026-05-18 | Items b/c/d/e FIXED

  b. FIXED — setUnlockedDeviceRequired(true) added to buildKeySpec() in SessionModule.kt.
     Key unusable while device is locked. API 28+; minSdk is 28, no guard needed.

  c. FIXED — setInvalidatedByBiometricEnrollment(true) added to buildKeySpec().
     Key permanently invalidated if new biometrics enrolled after key creation.
     buildKeysetManager() catches KeyPermanentlyInvalidatedException → wipes DataStore +
     keyset + key → regenerates → user re-authenticates at the auth gate.

  d. FIXED — KeyRotationManager (private object, SessionModule.kt) stores key creation epoch
     in travelmonk_key_meta SharedPreferences. performKeyRotation() triggers on cold start
     when key is older than 30 days: wipes DataStore + keyset, deletes key, regenerates.
     User re-authenticates silently (session is empty → auth gate → welcome screen).

  e. FIXED — backup_rules.xml (API 23-30) and data_extraction_rules.xml (API 31+) updated.
     Excluded from Cloud Backup, Device Transfer, and Auto Backup:
       sharedpref/travelmonk_tink_prefs.xml
       sharedpref/travelmonk_key_meta.xml
       file/datastore/travelmonk_session.db
