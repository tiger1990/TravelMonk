package com.travelmonk.core.network.security

import java.io.IOException

/**
 * Thrown when the server's certificate chain fails the pinning check.
 *
 * Wraps OkHttp's [javax.net.ssl.SSLPeerUnverifiedException] so the repository layer can
 * distinguish a pin failure (possible MITM attack or stale pins) from a generic network
 * error, and surface a security-specific message to the user instead of "no internet".
 */
class PinningFailure(
    val host: String,
    cause: Throwable
) : IOException("Certificate pinning failed for host: $host", cause)
