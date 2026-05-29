package com.travelmonk.feature.bookings.di

import com.travelmonk.feature.bookings.data.api.BookingsApi
import com.travelmonk.feature.bookings.data.repository.BookingRepositoryImpl
import com.travelmonk.feature.bookings.domain.repository.BookingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.travelmonk.core.network.di.PinnedRetrofit
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BookingModule {

    /**
     * Binds [BookingRepositoryImpl] as the [BookingRepository] implementation.
     * Resolved by Hilt/KSP at compile time — no direct call site exists in source.
     */
    @Suppress("unused")
    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository

    companion object {
        /**
         * Uses [@PinnedRetrofit] so all booking API calls go through the OkHttpClient
         * with SPKI certificate pinning enforced. This prevents MITM attacks on booking
         * confirmation, cancellation, and future payment endpoints on this host.
         * Any feature module targeting sensitive data must also use [@PinnedRetrofit].
         */
        @Provides
        @Singleton
        fun provideBookingsApi(@PinnedRetrofit retrofit: Retrofit): BookingsApi {
            return retrofit.create(BookingsApi::class.java)
        }
    }
}
