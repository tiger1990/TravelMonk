package com.travelmonk.core.network.security

/**
 * Abstraction over where the active pin configuration comes from.
 *
 * The transport layer ([CertificatePinnerFactory]) depends on this interface, not on a
 * concrete source — so [StaticPinSource] can be swapped for [RemotePinSource] (remote
 * rotation without an app update) by changing a single [@Binds] in [NetworkModule],
 * with zero changes to any other class.
 */
interface PinSource {
    fun currentConfig(): PinningConfig
}
