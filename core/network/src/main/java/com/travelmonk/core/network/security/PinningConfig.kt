package com.travelmonk.core.network.security

import com.travelmonk.core.common.config.Environment

/**
 * Immutable pin configuration keyed by environment.
 *
 * Each [HostPins] must carry a primary + at least one backup pin. The backup pin is the
 * SPKI hash of a key held in reserve (not yet serving traffic), enabling zero-downtime
 * cert rotation: the server switches to the backup key and every app already accepts it.
 *
 * All hashes are SHA-256 of the SubjectPublicKeyInfo (SPKI), prefixed `sha256/`.
 * SPKI survives certificate reissuance — a 90-day Let's Encrypt renewal with the same
 * key pair does NOT change the SPKI hash.
 */
data class PinningConfig(val hostPins: List<HostPins>) {

    companion object {
        // ⚠️  PLACEHOLDER hashes — replace with real openssl output before shipping.
        //     See documents/certificate_pinning.md for the extraction commands.
        private const val PROD_LEAF_PRIMARY = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        private const val PROD_LEAF_BACKUP  = "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
        private const val PROD_CA_BACKUP    = "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC="

        private const val STAGING_PRIMARY   = "sha256/DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD="
        private const val STAGING_BACKUP    = "sha256/EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE="

        fun forEnvironment(environment: Environment): PinningConfig = when (environment) {
            Environment.PRODUCTION -> PinningConfig(
                listOf(
                    HostPins(
                        hostPattern = "api.travelmonk.com",
                        pins = listOf(PROD_LEAF_PRIMARY, PROD_LEAF_BACKUP, PROD_CA_BACKUP)
                    )
                )
            )
            Environment.STAGING -> PinningConfig(
                listOf(
                    HostPins(
                        hostPattern = "staging-api.travelmonk.com",
                        pins = listOf(STAGING_PRIMARY, STAGING_BACKUP)
                    )
                )
            )
            // DEV: no pinning — Charles/mitmproxy must work for local debugging.
            // CertificatePinnerFactory returns CertificatePinner.DEFAULT for empty config.
            Environment.DEV -> PinningConfig(emptyList())
        }
    }
}

data class HostPins(val hostPattern: String, val pins: List<String>) {
    init {
        require(pins.size >= 2) {
            "Host '$hostPattern' requires a primary + at least one backup pin (got ${pins.size}). " +
            "A single pin bricks all requests on cert rotation."
        }
        require(pins.all { it.startsWith("sha256/") }) {
            "All pins for '$hostPattern' must use the OkHttp 'sha256/' prefix."
        }
    }
}
