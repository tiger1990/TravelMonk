package com.travelmonk.feature.flights.di

import com.travelmonk.feature.flights.data.remote.FlightsApi
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


    @Binds
    @Singleton
    abstract fun bindFlightRepository(
        flightRepositoryImpl: FlightRepositoryImpl
    ): FlightRepository

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
