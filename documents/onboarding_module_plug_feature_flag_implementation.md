# Plan: Onboarding Module + Feature Flag Architecture

This plan covers two tightly related pieces:
1. **`feature:onboarding`** — passwordless auth: phone + OTP login, passkey login/registration
2. **Feature Flag Production Architecture** — persistence, reactive Compose updates, SoC, Statsig-ready

Both are planned together because login is the natural trigger for `FeatureFlagSyncer.sync()`.

---

## References

| Resource | Link | Key Takeaway |
|----------|------|--------------|
| Android Passkey UX Guide | https://developer.android.com/design/ui/mobile/guides/patterns/passkeys | Credential Manager API, single-tap (Android 15), two-tap (≤14), UX copy guidelines |
| WebAuthn & Passkeys for Developers (Auth0) | https://auth0.com/blog/webauthn-and-passkeys-for-developers/ | Registration + authentication ceremony, challenge/response model |
| WebAuthn for Java Developers (Auth0) | https://auth0.com/blog/webauthn-and-passkeys-for-java-developers/ | Spring backend WebAuthn integration |
| Spring Security + JWT Backend | https://medium.com/@nitishkumarkushwaha43/building-a-production-ready-authentication-system-with-spring-security-and-jwt-cb1d86ca2e97 | JWT filter chain, token issuance after successful auth |

---

## Part A — Feature Flag Architecture

### Context

Three critical gaps in the current `AppFeatureFlags`:
1. **No persistence** — flags reset to `true` on cold start (flicker)
2. **Compose recomposition bug** — `remember(featureFlags)` in `TravelMonkApp` uses a singleton ref that never changes; bottom bar never updates post-login
3. **No SoC** — `updateFlags()` is public; any injector can mutate flags

### New Contracts (all in `core/common/config/`)

| File | Purpose |
|------|---------|
| `FeatureFlagsData.kt` | Immutable value type: `data class FeatureFlagsData(isTransport, isStays, isExperiences, isServices)` |
| `FeatureFlagStore.kt` | Read interface: `val flagsFlow: StateFlow<FeatureFlagsData>` |
| `FeatureFlagSyncer.kt` | Write interface: `suspend fun sync()` — only login calls this |

### Production Implementation

**Delete:** `core/common/config/AppFeatureFlags.kt`

**New:** `core/common/config/AppFeatureFlagStore.kt` — `@Singleton`, implements all three interfaces:
- `flagsFlow` backed by `DataStore<Preferences>` via `.stateIn(@ApplicationScope, SharingStarted.Eagerly, DEFAULT)`
- `FeatureFlags` plain-Boolean properties delegate to `flagsFlow.value.*`
- `sync()` calls the remote API; until backend exists → persists defaults (no flicker)
- DataStore keys: `flag_transport`, `flag_stays`, `flag_experiences`, `flag_services` (all Boolean)

**New:** `core/common/di/CoroutineModule.kt` — provides `@ApplicationScope CoroutineScope`

**Updated:** `core/common/di/ConfigModule.kt` — binds all three interfaces + provides `DataStore<Preferences>`

**Updated:** `gradle/libs.versions.toml` + `core/common/build.gradle.kts` — add `datastore-preferences:1.1.1`

### Compose Fix

**`app/.../MainActivity.kt`** — inject `FeatureFlagStore` instead of `FeatureFlags`

**`app/.../ui/TravelMonkApp.kt`**:
```kotlin
val flags by featureFlagStore.flagsFlow.collectAsStateWithLifecycle()
val bottomBarItems = remember(flags) { BottomBarItems(...) }
// flags is FeatureFlagsData (value type) — recomposes correctly when sync() updates it
```

### Test Support

**`core/testing/di/FakeFeatureFlagsModule.kt`** — add `FakeFeatureFlagStore` (in-memory `MutableStateFlow`) and `FakeFeatureFlagSyncer` (no-op) alongside existing `DefaultFeatureFlags`

### Statsig Migration Path (future — zero UI changes)
Create `StatSigFeatureFlagStore` implementing the same three interfaces → update two `@Binds` in `ConfigModule` → done.

---

## Part B — Onboarding Module (Passwordless Auth)

### Auth Strategy

Two and only two login methods — **no email/password, no forgot password**:

| Method | When Used | How it Works |
|--------|-----------|--------------|
| **Phone + OTP** | Primary for new users; fallback for all users | Enter phone → receive SMS OTP → verify → JWT issued |
| **Passkey** | Returning users who registered a passkey | Credential Manager bottom sheet → biometric/PIN → signed assertion → JWT issued |

**Passkey tech stack (Android):**
- `androidx.credentials:credentials` — Credential Manager API (Android 14+)
- `androidx.credentials:credentials-play-services-auth` — backward compat down to Android 9
- System shows a **native bottom sheet** — no custom passkey screen needed for auth
- WebAuthn under the hood: public/private key pair stored securely on device, server stores only public key
- Per Google UX guidelines: use Material passkey icon, label button "Sign in with a passkey"

### Passkey Ceremony Overview

```
REGISTRATION (shown once after first OTP login):
  App ──► POST /auth/passkey/register/begin    ──► server returns challenge JSON
  App ──► CredentialManager.createCredential() ──► device creates key pair, signs challenge
  App ──► POST /auth/passkey/register/complete ──► server stores public key + credentialId ──► JWT

AUTHENTICATION (returning user with passkey):
  App ──► POST /auth/passkey/auth/begin        ──► server returns challenge JSON
  App ──► CredentialManager.getCredential()    ──► biometric/PIN → device signs challenge
  App ──► POST /auth/passkey/auth/complete     ──► server verifies signature ──► JWT
```

> **Why two steps?** The WebAuthn spec mandates a server-generated random challenge per attempt to prevent replay attacks. Cannot be collapsed into one round-trip.

---

### Architecture Overview

```
feature/onboarding-api/          ← public contracts (nav keys, navigator interface)
feature/onboarding/              ← full implementation
  ui/                            ← 4 screens + ViewModels
  mvi/                           ← State, Intent, Effect per screen
  domain/model/                  ← User, AuthState, AuthToken
  domain/repository/             ← AuthRepository, PasskeyRepository (interfaces)
  domain/usecase/                ← SendOtpUseCase, VerifyOtpUseCase, PasskeyAuthUseCase, PasskeyRegistrationUseCase
  data/api/                      ← AuthApi (Retrofit — phone OTP + WebAuthn ceremonies)
  data/api/dto/                  ← phone DTOs + passkey ceremony DTOs
  data/mapper/                   ← AuthMapper.kt
  data/repository/               ← AuthRepositoryImpl, PasskeyRepositoryImpl
  data/local/                    ← UserSessionStore (DataStore-backed token + auth state)
  di/                            ← NavigatorModule, NavHandlerModule, NavigationModule, AuthModule, SessionModule
  navigation/                    ← OnboardingNavKeyHandler
```

---

### Nav Keys (`feature/onboarding-api`)

```kotlin
@Serializable sealed interface OnboardingNavKey : TravelNavKey {

    // Entry point — "Get Started" (phone flow) + "Sign in with a passkey" (Credential Manager)
    @Serializable @SerialName("onboarding.welcome")
    data object Welcome : OnboardingNavKey

    // Phone number entry with country code picker
    @Serializable @SerialName("onboarding.phone_entry")
    data object PhoneEntry : OnboardingNavKey

    // OTP verification — carries phone for display and resend
    @Serializable @SerialName("onboarding.otp")
    data class Otp(val phone: String) : OnboardingNavKey

    // Post-OTP prompt shown once per device: "Create a passkey for faster sign-in"
    @Serializable @SerialName("onboarding.passkey_prompt")
    data object PasskeyPrompt : OnboardingNavKey
}
```

**Removed vs original plan:** `Login` (email/pass), `SignUp` (email/pass), `ForgotPassword` — all eliminated.
Passkey *authentication* uses the system Credential Manager bottom sheet — no dedicated nav screen.

---

### Navigator Interface (`feature/onboarding-api`)

```kotlin
@Stable
interface OnboardingNavigator {
    fun toPhoneEntry()
    fun toOtp(phone: String)
    fun toPasskeyPrompt()
    fun back()
}
```

---

### Auth Gate in MainActivity

Hard gate — unauthenticated users never reach the main app.

```
AuthState.Loading         → splash stays on screen (no flash)
AuthState.Unauthenticated → OnboardingFlow (Welcome screen, no bottom nav)
AuthState.Authenticated   → TravelMonkApp (bottom nav, full app)
```

---

### Session Management (`feature/onboarding/data/local/`)

**`UserSessionStore.kt`** — `@Singleton`, backed by `DataStore<Preferences>`:

| DataStore Key | Type | Purpose |
|---------------|------|---------|
| `auth_token` | String | JWT access token |
| `refresh_token` | String | JWT refresh token |
| `user_id` | String | Authenticated user ID |
| `phone_number` | String | User's phone (E.164 format) |
| `passkey_registered` | Boolean | Whether passkey created on this device — controls PasskeyPrompt visibility |

```kotlin
interface UserSessionStore {
    val authStateFlow: StateFlow<AuthState>
    suspend fun saveSession(token: AuthToken, userId: String, phone: String)
    suspend fun markPasskeyRegistered()
    fun isPasskeyRegistered(): Boolean
    suspend fun clearSession()
}
```

**`AuthState.kt`** (sealed interface in `domain/model/`):
```kotlin
sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val userId: String) : AuthState
}
```

---

### Screens + MVI

#### 1. Welcome Screen
- **State**: `WelcomeState(isPasskeyLoading: Boolean = false, error: String? = null)`
- **Intents**: `GetStarted`, `SignInWithPasskey`
- **Effects**: `NavigateToPhoneEntry`, `NavigateToHome`, `ShowError`
- **UI**:
  - Full-screen hero image, TravelMonk logo
  - `"Get Started"` — primary CTA → phone entry flow
  - `"Sign in with a passkey"` — secondary CTA with Material passkey icon
- **ViewModel**: on `SignInWithPasskey` → `PasskeyAuthUseCase` → Credential Manager bottom sheet → on success: `featureFlagSyncer.sync()` + `NavigateToHome`

> **UX (per Google guidelines):** Don't list all sign-in options upfront. Passkey is the secondary option. Use "Sign in with a passkey" — not "Use biometrics". Credential Manager consolidates all passkeys automatically.

#### 2. Phone Entry Screen
- **State**: `PhoneEntryState(countryCode: String = "+91", phoneNumber: String = "", isLoading: Boolean = false, error: String? = null)`
- **Intents**: `CountryCodeChanged`, `PhoneNumberChanged`, `Submit`
- **Effects**: `NavigateToOtp(phone)`, `ShowError`
- **UI**: Country code picker + phone number field, "Send OTP" CTA
- **Validation**: E.164 format check before submission

#### 3. OTP Verification Screen
- **State**: `OtpState(phone: String, otp: String = "", isLoading: Boolean = false, error: String? = null, resendCooldownSeconds: Int = 30)`
- **Intents**: `OtpChanged`, `Submit`, `ResendOtp`
- **Effects**: `NavigateToPasskeyPrompt`, `NavigateToHome`, `ShowError`
- **UI**: 6-digit OTP input (auto-advance between cells), masked phone display, resend with countdown, "Verify" CTA
- **Post-verify logic**: if `isPasskeyRegistered() == false` → `NavigateToPasskeyPrompt`; else → `NavigateToHome`

#### 4. Passkey Prompt Screen *(shown once, after first successful OTP login on this device)*
- **State**: `PasskeyPromptState(isLoading: Boolean = false, error: String? = null)`
- **Intents**: `CreatePasskey`, `Skip`
- **Effects**: `NavigateToHome`, `ShowError`
- **UI**: Illustration, "Create a passkey for faster sign-in" headline (benefit-first per Google UX), "Create passkey" primary CTA, "Not now" skip link
- **ViewModel**: on `CreatePasskey` → `PasskeyRegistrationUseCase` → Credential Manager → on success: `markPasskeyRegistered()` → `NavigateToHome`; on `Skip` → `NavigateToHome` (passkey_registered stays false — user can create from settings later)

> **UX (per Google guidelines):** Promote passkey creation right after first login. Button label: "Create a passkey". Avoid over-explaining the technology — lead with the benefit (faster, more secure sign-in).

---

### Data Layer

#### API Endpoints (`AuthApi.kt`)

```kotlin
interface AuthApi {
    // ── Phone OTP ─────────────────────────────────────────────────────────
    @POST("auth/phone/send-otp")
    suspend fun sendOtp(@Body body: SendOtpRequestDto): Unit

    @POST("auth/phone/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequestDto): AuthResponseDto

    // ── Passkey Registration (WebAuthn — 2-step ceremony) ─────────────────
    @POST("auth/passkey/register/begin")
    suspend fun beginPasskeyRegistration(@Body body: PasskeyUserDto): PasskeyChallengeDto

    @POST("auth/passkey/register/complete")
    suspend fun completePasskeyRegistration(@Body body: PasskeyAttestationDto): AuthResponseDto

    // ── Passkey Authentication (WebAuthn — 2-step ceremony) ───────────────
    @POST("auth/passkey/auth/begin")
    suspend fun beginPasskeyAuth(@Body body: PasskeyAuthBeginDto): PasskeyChallengeDto

    @POST("auth/passkey/auth/complete")
    suspend fun completePasskeyAuth(@Body body: PasskeyAssertionDto): AuthResponseDto

    // ── Token Management ──────────────────────────────────────────────────
    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: RefreshTokenDto): AuthResponseDto
}
```

#### DTOs (`data/api/dto/`) — all `@Serializable` with `@SerialName` snake_case

```kotlin
// Phone OTP
data class SendOtpRequestDto(@SerialName("phone_number") val phoneNumber: String)
data class VerifyOtpRequestDto(
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("otp")          val otp: String
)

// Shared auth response (both phone and passkey paths return same shape)
data class AuthResponseDto(
    @SerialName("access_token")  val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user_id")       val userId: String,
    @SerialName("phone_number")  val phoneNumber: String,
    @SerialName("name")          val name: String
)

// Passkey — server challenge (used for both registration and authentication begin)
data class PasskeyChallengeDto(
    @SerialName("challenge")           val challenge: String,           // Base64url-encoded random bytes
    @SerialName("rp_id")               val rpId: String,                // e.g. "travelmonk.com"
    @SerialName("allowed_credentials") val allowedCredentials: List<String> = emptyList()
)

// Passkey registration
data class PasskeyUserDto(@SerialName("user_id") val userId: String)
data class PasskeyAttestationDto(
    @SerialName("credential_id")      val credentialId: String,
    @SerialName("client_data_json")   val clientDataJson: String,   // Base64url
    @SerialName("attestation_object") val attestationObject: String  // Base64url
)

// Passkey authentication
data class PasskeyAuthBeginDto(@SerialName("user_id") val userId: String? = null) // null = discoverable credential
data class PasskeyAssertionDto(
    @SerialName("credential_id")      val credentialId: String,
    @SerialName("client_data_json")   val clientDataJson: String,
    @SerialName("authenticator_data") val authenticatorData: String,
    @SerialName("signature")          val signature: String
)

data class RefreshTokenDto(@SerialName("refresh_token") val refreshToken: String)
```

#### Domain Models (`domain/model/`)

```kotlin
data class User(val id: String, val name: String, val phoneNumber: String)
data class AuthToken(val accessToken: String, val refreshToken: String)
```

#### Mapper (`data/mapper/AuthMapper.kt`)

```kotlin
fun AuthResponseDto.toAuthToken(): AuthToken = AuthToken(accessToken, refreshToken)
fun AuthResponseDto.toUser(): User = User(id = userId, name = name, phoneNumber = phoneNumber)
// PasskeyChallengeDto serialised to JSON string before passing to Credential Manager API
fun PasskeyChallengeDto.toRequestJson(): String = Json.encodeToString(this)
```

#### Repositories

**`AuthRepository`** (interface):
```kotlin
interface AuthRepository {
    suspend fun sendOtp(phoneNumber: String): DataResult<Unit>
    suspend fun verifyOtp(phoneNumber: String, otp: String): DataResult<Pair<AuthToken, User>>
    suspend fun refreshToken(refreshToken: String): DataResult<AuthToken>
}
```

**`PasskeyRepository`** (interface):
```kotlin
interface PasskeyRepository {
    // Returns challenge JSON string ready to pass to CredentialManager
    suspend fun beginRegistration(userId: String): DataResult<String>
    suspend fun completeRegistration(attestationResponseJson: String): DataResult<Pair<AuthToken, User>>
    suspend fun beginAuthentication(): DataResult<String>
    suspend fun completeAuthentication(assertionResponseJson: String): DataResult<Pair<AuthToken, User>>
}
```

**`AuthRepositoryImpl`** — fake data pattern:
```kotlin
override suspend fun verifyOtp(phoneNumber: String, otp: String): DataResult<Pair<AuthToken, User>> =
    withContext(ioDispatcher) {
        // TODO: Replace with real API call when backend is integrated:
        // val response = authApi.verifyOtp(VerifyOtpRequestDto(phoneNumber, otp))
        // DataResult.Success(response.toAuthToken() to response.toUser())
        DataResult.Success(
            AuthToken("fake_access_token", "fake_refresh_token") to
            User("user_001", "Traveler", phoneNumber)
        )
    }
```

#### Use Cases (`domain/usecase/`)

| UseCase | Orchestrates |
|---------|-------------|
| `SendOtpUseCase` | `AuthRepository.sendOtp(phoneNumber)` |
| `VerifyOtpUseCase` | `AuthRepository.verifyOtp()` → `UserSessionStore.saveSession()` |
| `PasskeyAuthUseCase` | `PasskeyRepository.beginAuthentication()` → `CredentialManager.getCredential()` → `PasskeyRepository.completeAuthentication()` → `UserSessionStore.saveSession()` |
| `PasskeyRegistrationUseCase` | `PasskeyRepository.beginRegistration()` → `CredentialManager.createCredential()` → `PasskeyRepository.completeRegistration()` → `UserSessionStore.markPasskeyRegistered()` |

---

### Credential Manager Integration (Android)

**New dependencies** in `feature/onboarding/build.gradle.kts`:
```kotlin
implementation(libs.androidx.credentials)
implementation(libs.androidx.credentials.play.services.auth) // backward compat Android 9-13
```

**New entries in `gradle/libs.versions.toml`:**
```toml
[versions]
credentials = "1.3.0"

[libraries]
androidx-credentials               = { group = "androidx.credentials", name = "credentials",                   version.ref = "credentials" }
androidx-credentials-play-services = { group = "androidx.credentials", name = "credentials-play-services-auth", version.ref = "credentials" }
```

**Hilt provision** in `AuthModule.kt`:
```kotlin
@Provides @Singleton
fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager =
    CredentialManager.create(context)
```

**Usage pattern in `PasskeyAuthUseCase`:**
```kotlin
// Step 1: get server challenge
val challengeJson = passkeyRepository.beginAuthentication().getOrThrow()

// Step 2: Credential Manager shows native biometric bottom sheet
val request = GetCredentialRequest(listOf(GetPublicKeyCredentialOption(requestJson = challengeJson)))
val result  = credentialManager.getCredential(context = activityContext, request = request)

// Step 3: send signed assertion to server → receive JWT
val credential = result.credential as PublicKeyCredential
passkeyRepository.completeAuthentication(credential.authenticationResponseJson)
```

> **Note:** `CredentialManager.getCredential()` requires an `Activity` context. Inject it via `@ActivityContext` in use cases called from ViewModels, or pass it as a parameter from the composable via an activity reference wrapper.

---

### Login → Feature Flag Sync (critical wiring)

Both auth paths call `featureFlagSyncer.sync()` before navigating home:

```kotlin
// WelcomeViewModel — passkey path
is DataResult.Success -> {
    featureFlagSyncer.sync()
    setEffect(WelcomeEffect.NavigateToHome)
}

// OtpViewModel — phone OTP path
is DataResult.Success -> {
    featureFlagSyncer.sync()
    val showPasskeyPrompt = !userSessionStore.isPasskeyRegistered()
    if (showPasskeyPrompt) setEffect(OtpEffect.NavigateToPasskeyPrompt)
    else setEffect(OtpEffect.NavigateToHome)
}
```

---

### DI Modules (`feature/onboarding/di/`)

| Module | Scope | Responsibility |
|--------|-------|----------------|
| `NavigatorModule` | Singleton | `OnboardingNavigator` impl delegating to `NavigationBus` |
| `NavHandlerModule` | Singleton | `OnboardingNavKeyHandler` via `@Binds @IntoSet` |
| `NavigationModule` | ActivityRetained | `NavEntryInstaller` for 4 screens via `@Provides @IntoSet` |
| `AuthModule` | Singleton | `AuthRepository`, `PasskeyRepository`, `AuthApi`, `CredentialManager` |
| `SessionModule` | Singleton | `UserSessionStore` + its `DataStore<Preferences>` |

---

### App Module Changes

**`MainActivity`**:
```kotlin
val authState by userSessionStore.authStateFlow.collectAsStateWithLifecycle()

when (authState) {
    AuthState.Loading          -> { /* splash stays via setKeepOnScreenCondition */ }
    AuthState.Unauthenticated  -> OnboardingFlow(navigator = onboardingNavigator)
    is AuthState.Authenticated -> TravelMonkApp(featureFlagStore = featureFlagStore, ...)
}
```

---

## Complete File List

### New — `feature/onboarding-api`
```
feature/onboarding-api/build.gradle.kts
feature/onboarding-api/AndroidManifest.xml
.../onboardingapi/navigation/OnboardingNavKey.kt     (Welcome, PhoneEntry, Otp, PasskeyPrompt)
.../onboardingapi/navigator/OnboardingNavigator.kt
```

### New — `feature/onboarding`
```
feature/onboarding/build.gradle.kts
feature/onboarding/AndroidManifest.xml
.../onboarding/mvi/WelcomeMvi.kt
.../onboarding/mvi/PhoneEntryMvi.kt
.../onboarding/mvi/OtpMvi.kt
.../onboarding/mvi/PasskeyPromptMvi.kt
.../onboarding/ui/WelcomeScreen.kt          + WelcomeViewModel.kt
.../onboarding/ui/PhoneEntryScreen.kt       + PhoneEntryViewModel.kt
.../onboarding/ui/OtpScreen.kt              + OtpViewModel.kt
.../onboarding/ui/PasskeyPromptScreen.kt    + PasskeyPromptViewModel.kt
.../onboarding/domain/model/User.kt
.../onboarding/domain/model/AuthState.kt
.../onboarding/domain/model/AuthToken.kt
.../onboarding/domain/repository/AuthRepository.kt
.../onboarding/domain/repository/PasskeyRepository.kt
.../onboarding/domain/usecase/SendOtpUseCase.kt
.../onboarding/domain/usecase/VerifyOtpUseCase.kt
.../onboarding/domain/usecase/PasskeyAuthUseCase.kt
.../onboarding/domain/usecase/PasskeyRegistrationUseCase.kt
.../onboarding/data/api/AuthApi.kt
.../onboarding/data/api/dto/SendOtpRequestDto.kt
.../onboarding/data/api/dto/VerifyOtpRequestDto.kt
.../onboarding/data/api/dto/AuthResponseDto.kt
.../onboarding/data/api/dto/PasskeyChallengeDto.kt
.../onboarding/data/api/dto/PasskeyAttestationDto.kt
.../onboarding/data/api/dto/PasskeyAuthBeginDto.kt
.../onboarding/data/api/dto/PasskeyAssertionDto.kt
.../onboarding/data/api/dto/RefreshTokenDto.kt
.../onboarding/data/mapper/AuthMapper.kt
.../onboarding/data/repository/AuthRepositoryImpl.kt
.../onboarding/data/repository/PasskeyRepositoryImpl.kt
.../onboarding/data/local/UserSessionStore.kt
.../onboarding/navigation/OnboardingNavKeyHandler.kt
.../onboarding/di/NavigatorModule.kt
.../onboarding/di/NavHandlerModule.kt
.../onboarding/di/NavigationModule.kt
.../onboarding/di/AuthModule.kt
.../onboarding/di/SessionModule.kt
```

### Modified — Core / Feature Flag
```
gradle/libs.versions.toml                     ← add datastore-preferences:1.1.1, credentials:1.3.0
core/common/build.gradle.kts                  ← add datastore dep
core/common/di/CoroutineModule.kt             ← NEW: @ApplicationScope CoroutineScope
core/common/config/FeatureFlagsData.kt        ← NEW: immutable data class
core/common/config/FeatureFlagStore.kt        ← NEW: read interface (StateFlow)
core/common/config/FeatureFlagSyncer.kt       ← NEW: write interface (suspend fun sync)
core/common/config/AppFeatureFlagStore.kt     ← NEW: DataStore-backed production impl
core/common/config/AppFeatureFlags.kt         ← DELETE
core/common/di/ConfigModule.kt                ← update bindings
core/testing/di/FakeFeatureFlagsModule.kt     ← add FakeFeatureFlagStore + FakeFeatureFlagSyncer
```

### Modified — App Module
```
settings.gradle.kts                           ← include onboarding + onboarding-api
app/build.gradle.kts                          ← add onboarding deps
app/.../MainActivity.kt                       ← auth gate + FeatureFlagStore injection
app/.../ui/TravelMonkApp.kt                   ← collect flagsFlow, fix remember key
app/.../ui/OnboardingFlow.kt                  ← NEW: standalone nav host composable
```

---

## Build Order

1. **`feature:onboarding-api`** — nav keys + navigator interface only
2. **`core:common`** — feature flag architecture
3. **`feature:onboarding`** — depends on onboarding-api + core:common
4. **`app`** — wires everything together

---

## Verification Checklist

| Scenario | Expected Behaviour |
|----------|--------------------|
| Cold start, no session | Welcome screen; "Get Started" + passkey sign-in option |
| Phone OTP — new user | Welcome → PhoneEntry → OTP → PasskeyPrompt → TravelMonkApp |
| Phone OTP — returning user (passkey registered) | Welcome → PhoneEntry → OTP → TravelMonkApp directly |
| Passkey sign-in — happy path | Credential Manager bottom sheet → biometric → TravelMonkApp |
| Passkey sign-in — no passkey on device | Credential Manager: no passkeys available → user falls back to phone |
| Passkey creation (PasskeyPrompt) | Credential Manager bottom sheet → biometric → `markPasskeyRegistered()` → TravelMonkApp |
| Passkey creation — skipped | `passkey_registered` stays false; user reaches TravelMonkApp; can create later from settings |
| Feature flag sync | Both auth paths call `sync()` before navigating home; `flagsFlow` emits → bottom bar recomposes |
| Session persists cold start | Token in DataStore → `AuthState.Authenticated` before first frame; no splash flicker |
| Logout | `clearSession()` → `Unauthenticated` → Welcome screen |
| `@HiltAndroidTest` | Fake repositories + `FakeFeatureFlagStore` + `FakeFeatureFlagSyncer`; no Credential Manager, no DataStore |
| Statsig migration | Swap `AppFeatureFlagStore` binding only; zero UI or auth changes |