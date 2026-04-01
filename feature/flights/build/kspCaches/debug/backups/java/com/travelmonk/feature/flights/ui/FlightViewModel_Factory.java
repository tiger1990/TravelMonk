package com.travelmonk.feature.flights.ui;

import com.travelmonk.feature.flights.domain.repository.FlightRepository;
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
public final class FlightViewModel_Factory implements Factory<FlightViewModel> {
  private final Provider<FlightRepository> flightRepositoryProvider;

  private FlightViewModel_Factory(Provider<FlightRepository> flightRepositoryProvider) {
    this.flightRepositoryProvider = flightRepositoryProvider;
  }

  @Override
  public FlightViewModel get() {
    return newInstance(flightRepositoryProvider.get());
  }

  public static FlightViewModel_Factory create(
      Provider<FlightRepository> flightRepositoryProvider) {
    return new FlightViewModel_Factory(flightRepositoryProvider);
  }

  public static FlightViewModel newInstance(FlightRepository flightRepository) {
    return new FlightViewModel(flightRepository);
  }
}
