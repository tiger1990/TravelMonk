package com.travelmonk.core.network.security

import okhttp3.CertificatePinner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds an OkHttp [CertificatePinner] from the active [PinSource].
 *
 * Returns [CertificatePinner.DEFAULT] (a no-op pinner that accepts all connections) when
 * the config is empty — i.e. in the DEV environment. This means the same OkHttp wiring
 * works across all build flavors without any branching at call sites.
 */
@Singleton
class CertificatePinnerFactory @Inject constructor(
    private val pinSource: PinSource
) {
    fun create(): CertificatePinner {
        val config = pinSource.currentConfig()
        if (config.hostPins.isEmpty()) return CertificatePinner.DEFAULT

        return CertificatePinner.Builder()
            .apply {
                config.hostPins.forEach { host ->
                    add(host.hostPattern, *host.pins.toTypedArray())
                }
            }
            .build()
    }
}
