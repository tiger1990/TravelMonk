package com.travelmonk.core.network.security

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * OkHttp interceptor that translates [SSLPeerUnverifiedException] into a typed [PinningFailure].
 *
 * Added ONLY to the @PinnedClient. The default client is unaffected.
 * Coroutine safety is preserved — only the specific SSL exception is caught;
 * [kotlinx.coroutines.CancellationException] is never intercepted.
 */
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
