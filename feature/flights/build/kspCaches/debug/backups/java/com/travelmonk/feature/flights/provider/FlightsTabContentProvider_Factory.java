package com.travelmonk.feature.flights.provider;

import com.travelmonk.feature.flights.navigator.FlightNavigator;
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
public final class FlightsTabContentProvider_Factory implements Factory<FlightsTabContentProvider> {
  private final Provider<FlightNavigator> flightNavigatorProvider;

  private FlightsTabContentProvider_Factory(Provider<FlightNavigator> flightNavigatorProvider) {
    this.flightNavigatorProvider = flightNavigatorProvider;
  }

  @Override
  public FlightsTabContentProvider get() {
    return newInstance(flightNavigatorProvider.get());
  }

  public static FlightsTabContentProvider_Factory create(
      Provider<FlightNavigator> flightNavigatorProvider) {
    return new FlightsTabContentProvider_Factory(flightNavigatorProvider);
  }

  public static FlightsTabContentProvider newInstance(FlightNavigator flightNavigator) {
    return new FlightsTabContentProvider(flightNavigator);
  }
}
