package com.travelmonk.feature.flights.data.repository;

import com.travelmonk.feature.flights.data.remote.FlightsApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class FlightRepositoryImpl_Factory implements Factory<FlightRepositoryImpl> {
  private final Provider<FlightsApi> flightsApiProvider;

  private FlightRepositoryImpl_Factory(Provider<FlightsApi> flightsApiProvider) {
    this.flightsApiProvider = flightsApiProvider;
  }

  @Override
  public FlightRepositoryImpl get() {
    return newInstance(flightsApiProvider.get());
  }

  public static FlightRepositoryImpl_Factory create(Provider<FlightsApi> flightsApiProvider) {
    return new FlightRepositoryImpl_Factory(flightsApiProvider);
  }

  public static FlightRepositoryImpl newInstance(FlightsApi flightsApi) {
    return new FlightRepositoryImpl(flightsApi);
  }
}
