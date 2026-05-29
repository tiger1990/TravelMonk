# Certificate Pinning — Booking & Payment Endpoints
Last updated: 2026-05-29
Gap reference: P3-06 (ArchitectureGaps G-03)

This document is the permanent reference for TravelMonk's certificate pinning implementation.
It covers the threat model, architecture, all new files, the rotation strategy, and verification steps.

---

## Why This Matters

TravelMonk processes flight bookings, hotel reservations, and experience purchases. Without
certificate pinning, an attacker on the same network (hotel Wi-Fi, airport, coffee shop) can
intercept API traffic using a device-trusted certificate — reading auth tokens, modifying prices,
or manipulating booking confirmations silently. Certificate pinning makes this impossible by
refusing TLS handshakes whose server public key is not in an approved set.

### How Big Companies Solve This

| Company | Approach |
|---------|----------|
| **Stripe** | Pins only its own payment SDK host; general app traffic unpinned — zero blast radius on cert rotation |
| **Lyft** | Two OkHttp clients: default for general APIs, pinned for payment/auth; pins baked into the binary with backup pins for rotation |
| **Uber** | Certificate pinning + `network_security_config.xml` as dual-layer; no cleartext anywhere; separate pinned Retrofit for payment |
| **PayPal** | SPKI hash (not cert fingerprint) so cert can be reissued freely; always ≥ 2 pins; signed remote manifest for emergency rotation |
| **MakeMyTrip** | OkHttp `CertificatePinner` on booking/flight endpoints; separate Android NSC config per flavor |
| **TravelMonk** | Same 5-layer approach (see below) |

**Why SPKI hash (`sha256/`), not cert fingerprint:**
SPKI (Subject Public Key Info) is the hash of the public *key*, not the certificate. When Let's
Encrypt renews the cert every 90 days using the same key pair, the SPKI hash stays the same.
A fingerprint pin would break on every renewal. OkHttp only supports SPKI for exactly this reason.

---

## Architecture: 5 Layers of Defense

```
┌────────────────────────────────────────────────────────────────────────┐
│ Layer 1 — OS      network_security_config.xml                          │
│   Declarative backstop. Blocks cleartext traffic everywhere.           │
│   Pins production and staging hosts per flavor. Fails-open after       │
│   expiry (degrades to system trust — a forgotten update does not       │
│   brick the app).                                                       │
├────────────────────────────────────────────────────────────────────────┤
│ Layer 2 — Transport   OkHttp CertificatePinner                         │
│   The enforcing control. Fails-closed on mismatch (no expiry).         │
│   Env-aware: DEV = no-op, STAGING = staging pins, PROD = prod pins.    │
├────────────────────────────────────────────────────────────────────────┤
│ Layer 3 — Routing     @PinnedClient vs @DefaultClient                  │
│   Two separate OkHttpClient instances. Booking/payment features use    │
│   the pinned client only. Image CDN, analytics, etc. stay unpinned.    │
│   Confines blast radius to sensitive endpoints.                        │
├────────────────────────────────────────────────────────────────────────┤
│ Layer 4 — Config      PinningConfig + PinSource                        │
│   Environment-aware pin sets. StaticPinSource is the default (pins     │
│   baked into the binary). RemotePinSource stub supports emergency      │
│   rotation without an app update.                                      │
├────────────────────────────────────────────────────────────────────────┤
│ Layer 5 — Failure     PinFailureInterceptor + PinningFailure           │
│   Translates OkHttp's low-level SSLPeerUnverifiedException into a      │
│   typed PinningFailure so the repository layer can show a              │
│   security-specific error message, not a generic network error.        │
└────────────────────────────────────────────────────────────────────────┘
```

**Key design decision — two OkHttpClients (Layer 3):**
Pinning is a liability as much as a protection. A stale pin hard-fails every request that uses
the pinned client. By confining it to booking/payment, a pin rotation incident does not knock out
your image CDN or analytics — it only affects the endpoints that warrant it. This is exactly how
Stripe and Adyen structure their payment SDKs.

---

## Files Created / Modified

```
core/network/src/main/java/com/travelmonk/core/network/
├── di/
│   ├── NetworkModule.kt              ← MODIFIED (object → abstract class; two clients + Retrofits)
│   └── NetworkQualifiers.kt          ← NEW
└── security/
    ├── PinningConfig.kt              ← NEW
    ├── CertificatePinnerFactory.kt   ← NEW
    ├── PinSource.kt                  ← NEW
    ├── StaticPinSource.kt            ← NEW
    ├── RemotePinSource.kt            ← NEW (stub — remote rotation; additive merge with safe fallback)
    ├── PinningFailure.kt             ← NEW
    └── PinFailureInterceptor.kt      ← NEW

core/network/build.gradle.kts         ← MODIFIED (add :core:logger for RemotePinSource logging)

app/src/main/res/xml/
└── network_security_config.xml       ← NEW (Layer 1 backstop + cleartext block — production)

app/src/dev/res/xml/
└── network_security_config.xml       ← NEW (dev flavor override — allows user CAs for Charles)

app/src/staging/res/xml/
└── network_security_config.xml       ← NEW (staging flavor override — pins staging-api.travelmonk.com)

app/src/main/AndroidManifest.xml      ← MODIFIED (add android:networkSecurityConfig attribute)
feature/bookings/.../di/BookingModule.kt  ← MODIFIED (@PinnedRetrofit instead of bare Retrofit)
```

---

## Source Code

### `NetworkQualifiers.kt`

Typed `@Qualifier` annotations. Type-safe — a typo is a compile error, not a runtime crash.
Consistent with existing `@IoDispatcher` pattern in the project.

```kotlin
package com.travelmonk.core.network.di

import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultClient
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class PinnedClient
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class PinnedRetrofit
```

---

### `PinningConfig.kt`

Environment-aware. `HostPins.init {}` enforces ≥ 2 pins at construction time — the backup pin
rule cannot be forgotten. Placeholder hashes marked ⚠️.

```kotlin
package com.travelmonk.core.network.security

import com.travelmonk.core.common.config.Environment

data class PinningConfig(val hostPins: List<HostPins>) {
    companion object {
        // ⚠️ PLACEHOLDERS — replace with real openssl output before shipping (see section below)
        private const val PROD_LEAF_PRIMARY = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        private const val PROD_LEAF_BACKUP  = "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
        private const val PROD_CA_BACKUP    = "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC="

        private const val STAGING_PRIMARY   = "sha256/DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD="
        private const val STAGING_BACKUP    = "sha256/EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE="

        fun forEnvironment(environment: Environment): PinningConfig = when (environment) {
            Environment.PRODUCTION -> PinningConfig(listOf(
                HostPins("api.travelmonk.com",
                    listOf(PROD_LEAF_PRIMARY, PROD_LEAF_BACKUP, PROD_CA_BACKUP))
            ))
            Environment.STAGING -> PinningConfig(listOf(
                HostPins("staging-api.travelmonk.com",
                    listOf(STAGING_PRIMARY, STAGING_BACKUP))
            ))
            // DEV: no pinning — Charles/mitmproxy must work for local debugging
            Environment.DEV -> PinningConfig(emptyList())
        }
    }
}

data class HostPins(val hostPattern: String, val pins: List<String>) {
    init {
        require(pins.size >= 2) {
            "Host '$hostPattern' needs primary + backup pin (got ${pins.size}). " +
            "A single pin bricks the app on cert rotation."
        }
        require(pins.all { it.startsWith("sha256/") }) {
            "All pins for '$hostPattern' must use the OkHttp 'sha256/' prefix."
        }
    }
}
```

---

### `PinSource.kt`

```kotlin
package com.travelmonk.core.network.security

interface PinSource {
    fun currentConfig(): PinningConfig
}
```

---

### `StaticPinSource.kt`

Pins baked into the binary, selected by `AppConfig.environment`. Default `@Binds` impl.
Even when `RemotePinSource` is active, the static baseline is always the floor.

```kotlin
package com.travelmonk.core.network.security

import com.travelmonk.core.common.config.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaticPinSource @Inject constructor(private val appConfig: AppConfig) : PinSource {
    override fun currentConfig(): PinningConfig =
        PinningConfig.forEnvironment(appConfig.environment)
}
```

---

### `RemotePinSource.kt`

Emergency rotation without an app update. Until the backend is built, `fetchVerifiedRemotePins()`
returns `null` and the static baseline is the sole source of truth.

**Merge rule:** additive only — remote can never remove shipped baseline pins. If a remote entry
fails `HostPins` validation (e.g. a new host with only 1 pin), it is skipped and logged rather
than crashing. Remote data is untrusted; the baseline hosts are always structurally valid.

Requires `implementation(project(":core:logger"))` in `core/network/build.gradle.kts`.

```kotlin
package com.travelmonk.core.network.security

import com.travelmonk.core.logger.TravelMonkLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemotePinSource @Inject constructor(private val staticSource: StaticPinSource) : PinSource {

    override fun currentConfig(): PinningConfig {
        val baseline = staticSource.currentConfig()
        val remote = fetchVerifiedRemotePins() ?: return baseline
        return mergeAdditive(baseline, remote)
    }

    // TODO: fetch signed manifest from backend, verify signature with baked-in public key,
    // parse pin set. The signing key is NEVER itself rotated remotely.
    // Returns null until backend is implemented — falls back to static baseline.
    private fun fetchVerifiedRemotePins(): PinningConfig? = null

    // Union merge: remote can only widen the accepted set, never shrink it.
    // Invalid remote entries (e.g. single-pin hosts) are skipped and reported.
    private fun mergeAdditive(baseline: PinningConfig, remote: PinningConfig): PinningConfig {
        val byHost = LinkedHashMap<String, MutableList<String>>()
        (baseline.hostPins + remote.hostPins).forEach { host ->
            byHost.getOrPut(host.hostPattern) { mutableListOf() }
                .apply { host.pins.forEach { if (it !in this) add(it) } }
        }
        return PinningConfig(
            byHost.mapNotNull { (pattern, pins) ->
                runCatching { HostPins(pattern, pins) }
                    .onFailure { e ->
                        TravelMonkLogger.e(
                            tag = "RemotePinSource",
                            msg = "Skipping invalid remote pin entry for host '$pattern' — ${e.message}",
                            throwable = e
                        )
                    }
                    .getOrNull()
            }
        )
    }
}
```

**What the logger does when `runCatching` catches a bad remote entry:**
- `TravelMonkLogger.e(...)` writes to the local log file via `LogProcessor`
- Because it is `LogLevel.ERROR`, it is also forwarded to `CriticalEventUploader` automatically
  (see `TravelMonkLogger` line 151) — the event reaches your remote crash reporter (Datadog /
  Firebase, whichever `RemoteLogSender` is wired in `LoggerModule`)
- The full throwable stacktrace is captured via `LogEvent.formatThrowable(throwable)`

---

### `CertificatePinnerFactory.kt`

Returns `CertificatePinner.DEFAULT` (no-op) for empty config (DEV). Same code path for all
build flavors — no flavor branching in Kotlin needed.

```kotlin
package com.travelmonk.core.network.security

import okhttp3.CertificatePinner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificatePinnerFactory @Inject constructor(private val pinSource: PinSource) {
    fun create(): CertificatePinner {
        val config = pinSource.currentConfig()
        if (config.hostPins.isEmpty()) return CertificatePinner.DEFAULT
        return CertificatePinner.Builder()
            .apply { config.hostPins.forEach { host -> add(host.hostPattern, *host.pins.toTypedArray()) } }
            .build()
    }
}
```

---

### `PinningFailure.kt`

Typed exception for MITM/stale-pin failures. Lets repositories distinguish a security failure
from a generic network timeout and show the right message.

```kotlin
package com.travelmonk.core.network.security

import java.io.IOException

class PinningFailure(val host: String, cause: Throwable) :
    IOException("Certificate pinning failed for host: $host", cause)
```

---

### `PinFailureInterceptor.kt`

Added **only** to `@PinnedClient`. Catches `SSLPeerUnverifiedException`, wraps as `PinningFailure`.
`CancellationException` is never caught — coroutine safety is preserved. `@Singleton` scopes the
interceptor to match the `@Singleton` `OkHttpClient` that holds it.

```kotlin
package com.travelmonk.core.network.security

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLPeerUnverifiedException

@Singleton
class PinFailureInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        } catch (e: SSLPeerUnverifiedException) {
            throw PinningFailure(host = request.url.host, cause = e)
        }
    }
}
```

---

### `NetworkModule.kt` (modified — key diffs)

`object` → `abstract class` (Hilt requires this to combine `@Binds` + `@Provides`).

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds @Singleton
    abstract fun bindPinSource(impl: StaticPinSource): PinSource

    companion object {

        // ... provideMoshi(), provideLoggingInterceptor() unchanged ...

        @Provides @Singleton
        fun provideOkHttpClient(logging: HttpLoggingInterceptor, appConfig: AppConfig): OkHttpClient {
            val t = appConfig.apiTimeoutSeconds.toLong()
            return OkHttpClient.Builder()
                .connectTimeout(t, TimeUnit.SECONDS).readTimeout(t, TimeUnit.SECONDS)
                .writeTimeout(t, TimeUnit.SECONDS).addInterceptor(logging).build()
        }

        @Provides @Singleton @PinnedClient
        fun providePinnedOkHttpClient(
            logging: HttpLoggingInterceptor,
            pinnerFactory: CertificatePinnerFactory,
            pinFailureInterceptor: PinFailureInterceptor,
            appConfig: AppConfig
        ): OkHttpClient {
            val t = appConfig.apiTimeoutSeconds.toLong()
            return OkHttpClient.Builder()
                .connectTimeout(t, TimeUnit.SECONDS).readTimeout(t, TimeUnit.SECONDS)
                .writeTimeout(t, TimeUnit.SECONDS)
                .certificatePinner(pinnerFactory.create())   // no-op in DEV, enforced in STAGING/PROD
                .addInterceptor(pinFailureInterceptor)
                .addInterceptor(logging).build()
        }

        @Provides @Singleton
        fun provideRetrofit(client: OkHttpClient, moshi: Moshi, appConfig: AppConfig): Retrofit =
            Retrofit.Builder().baseUrl(appConfig.baseUrl).client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi)).build()

        @Provides @Singleton @PinnedRetrofit
        fun providePinnedRetrofit(@PinnedClient client: OkHttpClient, moshi: Moshi, appConfig: AppConfig): Retrofit =
            Retrofit.Builder().baseUrl(appConfig.baseUrl).client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi)).build()
    }
}
```

---

### `BookingModule.kt` (single-line change)

```kotlin
// Before:
fun provideBookingsApi(retrofit: Retrofit): BookingsApi

// After:
fun provideBookingsApi(@PinnedRetrofit retrofit: Retrofit): BookingsApi
```

---

### Network Security Config files

#### Production — `app/src/main/res/xml/network_security_config.xml`

Layer 1 backstop for production builds. Keep pin digests in sync with `PinningConfig.kt`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors><certificates src="system" /></trust-anchors>
    </base-config>
    <!-- Fails-open after expiration (degrades to system trust, not a hard block). -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="false">api.travelmonk.com</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</pin> <!-- ⚠️ leaf primary -->
            <pin digest="SHA-256">BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=</pin> <!-- ⚠️ backup key -->
            <pin digest="SHA-256">CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=</pin> <!-- ⚠️ intermediate CA -->
        </pin-set>
    </domain-config>
</network-security-config>
```

#### Staging — `app/src/staging/res/xml/network_security_config.xml`

Overrides the main NSC for staging builds. Completes Layer 1 for the staging host — without this,
staging builds used the production NSC which has no pin for `staging-api.travelmonk.com`, leaving
the two-layer defense incomplete for staging. Keep pin digests in sync with `STAGING_PRIMARY` /
`STAGING_BACKUP` in `PinningConfig.kt`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors><certificates src="system" /></trust-anchors>
    </base-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="false">staging-api.travelmonk.com</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD=</pin> <!-- ⚠️ staging primary -->
            <pin digest="SHA-256">EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE=</pin> <!-- ⚠️ staging backup -->
        </pin-set>
    </domain-config>
</network-security-config>
```

#### Dev — `app/src/dev/res/xml/network_security_config.xml`

Allows user-installed CA certificates so Charles Proxy and mitmproxy work locally.
OkHttp `CertificatePinner` is also a no-op in DEV (`PinningConfig` returns empty config).

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
            <certificates src="system" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

**AndroidManifest.xml** — add to `<application>`:
```xml
android:networkSecurityConfig="@xml/network_security_config"
```

---

## Pin Rotation Strategy

### The multi-pin rule (why ≥ 2 pins is non-negotiable)

OkHttp accepts a connection if **any** pinned hash matches the server's certificate chain.

```
Steady state:  pins = [leafKeyA, backupKeyB]   server uses key A  →  app accepts
Rotation day:  server switches to key B         →  app still accepts (B matches)
Next release:  pins = [leafKeyB, newBackupKeyC]  invariant restored
```

Shipping with one pin = guaranteed brick on cert rotation. The `HostPins.init {}` check prevents
a single-pin entry from being accepted at runtime. The `mergeAdditive` safe-skip with
`runCatching` + `mapNotNull` ensures a misconfigured remote payload cannot crash the app even if
the backend sends a single-pin host — it is silently dropped and reported.

### Emergency path — `RemotePinSource`

1. Backend publishes a signed JSON pin manifest
2. App verifies with a baked-in signing public key (this key is **never** remotely changed)
3. New pins are union-merged with the static baseline (additive only — never removes shipped pins)
4. Invalid remote entries are skipped and reported to the crash reporter, not thrown
5. Until built: `fetchVerifiedRemotePins()` returns `null`, static baseline is sole source of truth

---

## Extracting Real SPKI Hashes

Run these before shipping to production:

```bash
# Leaf cert SPKI — primary pin
openssl s_client -servername api.travelmonk.com -connect api.travelmonk.com:443 </dev/null 2>/dev/null \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform der \
  | openssl dgst -sha256 -binary | openssl enc -base64
# Result: prefix with sha256/

# Intermediate CA SPKI — rotation-safe backup
openssl s_client -servername api.travelmonk.com -connect api.travelmonk.com:443 \
  -showcerts </dev/null 2>/dev/null \
  | awk '/BEGIN CERT/{c++} c==2' \
  | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der \
  | openssl dgst -sha256 -binary | openssl enc -base64

# Quick validation: set a wrong pin intentionally, run, read logcat SSLPeerUnverifiedException
# — OkHttp prints the expected pin values in the exception message.
```

Repeat for `staging-api.travelmonk.com` to extract the staging SPKI values (D/E slots).

---

## Repository Error Handling

When backend is live, catch `PinningFailure` in `BookingRepositoryImpl`:

```kotlin
.catch { e ->
    val message = when (e) {
        is PinningFailure ->
            "Your connection couldn't be verified securely. Please update the app."
        else -> "Unable to load bookings."
    }
    emit(DataResult.Error(e, message))
}
```

---

## Build Flavor Behavior

| | DEV | STAGING | PRODUCTION |
|---|---|---|---|
| OkHttp pinning (Layer 2) | None (`DEFAULT`) | Enforced (2 staging pins) | Enforced (3 pins: leaf + backup + CA) |
| NSC Layer 1 | User CAs allowed (Charles works) | Strict + staging pin-set | Strict + prod pin-set |
| Cleartext | Localhost only | Blocked | Blocked |
| Pin failure | Never triggers | Hard fail → `PinningFailure` | Hard fail → `PinningFailure` |
| Invalid remote pin entry | N/A | Skipped + logged to crash reporter | Skipped + logged to crash reporter |

---

## Verification Checklist

- [ ] `./gradlew :core:network:compileDebugKotlin` — clean compile
- [ ] `./gradlew :feature:bookings:compileDebugKotlin` — `@PinnedRetrofit` resolves
- [ ] Remove `@PinnedRetrofit` from `BookingModule` → build fails at KSP (type-safety proof)
- [ ] Unit: `CertificatePinnerFactory` returns `DEFAULT` for DEV, real pinner for STAGING/PROD
- [ ] Unit: `PinFailureInterceptor` maps `SSLPeerUnverifiedException` → `PinningFailure` with correct host
- [ ] Unit: `RemotePinSource.mergeAdditive` — remote can add pins, never remove baseline pins
- [ ] Unit: `RemotePinSource.mergeAdditive` — single-pin remote host is skipped, not crashed
- [ ] Unit: `HostPins` init throws on single pin
- [ ] DEV build: Charles proxy intercepts booking requests (no-op pinning confirmed)
- [ ] STAGING: wrong placeholder hash → logcat shows expected SPKI → confirms correct extraction
- [ ] STAGING: `app/src/staging/res/xml/network_security_config.xml` resolves `staging-api.travelmonk.com`
- [ ] Replace all ⚠️ placeholder hashes in `PinningConfig.kt` and all three `network_security_config.xml` files with real `openssl` output before production release

---

## Completion Status

| File | Description | Status |
|------|-------------|--------|
| `NetworkQualifiers.kt` | Typed qualifier annotations | DONE |
| `PinningConfig.kt` | Env-aware pin config with ≥ 2 pin enforcement | DONE |
| `PinSource.kt` + `StaticPinSource.kt` | Pin source abstraction and static impl | DONE |
| `RemotePinSource.kt` | Rotation stub — `@Singleton`, additive merge, safe skip on invalid remote entries | DONE |
| `CertificatePinnerFactory.kt` | Builds `CertificatePinner`; no-op in DEV | DONE |
| `PinningFailure.kt` + `PinFailureInterceptor.kt` | Typed failure mapping; `@Singleton` scoped | DONE |
| `NetworkModule.kt` | Two clients, two Retrofits, `@Binds PinSource` | DONE |
| `BookingModule.kt` | `@PinnedRetrofit` qualifier | DONE |
| `app/src/main/res/xml/network_security_config.xml` | Layer 1 backstop + cleartext block (production) | DONE |
| `app/src/staging/res/xml/network_security_config.xml` | Layer 1 backstop for staging host | DONE |
| `app/src/dev/res/xml/network_security_config.xml` | Dev override — user CAs for Charles Proxy | DONE |
| `app/src/main/AndroidManifest.xml` | `android:networkSecurityConfig` wired | DONE |
| `core/network/build.gradle.kts` | Add `:core:logger` dependency | DONE |
| Replace ⚠️ SPKI placeholders | Real `openssl` output per environment | **PENDING** |
| Unit tests | All new security classes covered | **PENDING** |