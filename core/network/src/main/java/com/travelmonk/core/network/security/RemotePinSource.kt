package com.travelmonk.core.network.security

import com.travelmonk.core.logger.TravelMonkLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub [PinSource] for emergency pin rotation without shipping an app update.
 *
 * Rotation strategy (to implement when backend is ready):
 *  1. Backend publishes a SIGNED pin manifest (JSON) at a dedicated endpoint.
 *  2. App fetches it on launch, verifies the signature using a baked-in public key.
 *     The signing key itself is NEVER remotely rotated — that would defeat trust.
 *  3. Verified remote pins are ADDITIVELY merged with the static baseline.
 *     Remote can only ADD pins — it can never remove the shipped baseline pins.
 *     A malicious or empty remote payload therefore cannot strip protection.
 *  4. Cache the verified manifest in EncryptedSharedPreferences with a TTL.
 *     On TTL expiry with no fresh fetch, fall back to the static baseline.
 *
 * Until the backend exists this delegates entirely to [StaticPinSource].
 * To activate: change the @Binds in NetworkModule from StaticPinSource to RemotePinSource.
 */
@Singleton
class RemotePinSource @Inject constructor(
    private val staticSource: StaticPinSource
) : PinSource {

    override fun currentConfig(): PinningConfig {
        val baseline = staticSource.currentConfig()
        val remote = fetchVerifiedRemotePins() ?: return baseline
        return mergeAdditive(baseline, remote)
    }

    // TODO: fetch signed manifest, verify signature, parse pin set.
    private fun fetchVerifiedRemotePins(): PinningConfig? = null

    // Remote data is untrusted. A misconfigured backend payload (or an attacker who bypasses
    // signature verification) could supply a single-pin host and crash all pinned requests —
    // a self-inflicted DoS on the payment/booking flow. runCatching skips invalid entries
    // instead of crashing, so the baseline pins for other hosts remain active.
    private fun mergeAdditive(baseline: PinningConfig, remote: PinningConfig): PinningConfig {
        val byHost = LinkedHashMap<String, MutableList<String>>()
        (baseline.hostPins + remote.hostPins).forEach { host ->
            byHost.getOrPut(host.hostPattern) { mutableListOf() }
                .apply { host.pins.forEach { pin -> if (pin !in this) add(pin) } }
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
