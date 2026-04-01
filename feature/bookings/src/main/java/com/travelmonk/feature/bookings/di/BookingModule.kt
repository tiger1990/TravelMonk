package com.travelmonk.feature.bookings.di

import com.travelmonk.feature.bookings.data.repository.BookingRepositoryImpl
import com.travelmonk.feature.bookings.domain.repository.BookingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BookingModule {

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository
}
