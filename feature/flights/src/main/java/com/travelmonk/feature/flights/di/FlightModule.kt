package com.travelmonk.feature.flights.di

import com.travelmonk.feature.flights.data.api.FlightsApi
import com.travelmonk.feature.flights.data.repository.FlightRepositoryImpl
import com.travelmonk.feature.flights.domain.repository.FlightRepository
import dagger.Binds
import dagger.multibindings.IntoSet
import com.travelmonk.feature.transportapi.TransportTabContentProvider
import com.travelmonk.feature.flights.provider.FlightsTabContentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FlightModule {


    /**
     * Binds [FlightRepositoryImpl] as the [FlightRepository] implementation.
     * Resolved by Hilt/KSP at compile time — no direct call site exists in source.
     */
    @Suppress("unused")
    @Binds
    @Singleton
    abstract fun bindFlightRepository(
        flightRepositoryImpl: FlightRepositoryImpl
    ): FlightRepository

    /**
     * Contributes [FlightsTabContentProvider] to the [TransportTabContentProvider] multibinding set.
     * Resolved by Hilt/KSP at compile time — no direct call site exists in source.
     */
    @Suppress("unused")
    @Binds
    @IntoSet
    abstract fun bindFlightsTabContentProvider(
        flightsTabContentProvider: FlightsTabContentProvider
    ): TransportTabContentProvider

    companion object {
        @Provides
        @Singleton
        fun provideFlightsApi(retrofit: Retrofit): FlightsApi {
            return retrofit.create(FlightsApi::class.java)
        }
    }
}
