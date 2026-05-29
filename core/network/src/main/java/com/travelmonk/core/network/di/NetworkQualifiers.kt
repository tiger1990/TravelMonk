package com.travelmonk.core.network.di

import javax.inject.Qualifier

/**
 * Hilt qualifier annotations for the two-client network architecture.
 *
 * [PinnedClient]   — OkHttpClient with SPKI certificate pinning enforced.
 *                    Use for booking, payment, and auth endpoints.
 *                    Fails-closed on pin mismatch; no-op in DEV flavor.
 *
 * [PinnedRetrofit] — Retrofit instance backed by [PinnedClient].
 *                    Inject this in any feature DI module whose API interface
 *                    targets a sensitive endpoint (e.g. BookingModule, future PaymentModule).
 *
 * Features that do NOT handle sensitive data (home, flights, stays, experiences) inject
 * the unqualified OkHttpClient / Retrofit — no annotation needed.
 */

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PinnedClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PinnedRetrofit
