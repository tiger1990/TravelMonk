package com.travelmonk.feature.flights.di;

import com.travelmonk.feature.flights.data.remote.FlightsApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
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
public final class FlightModule_Companion_ProvideFlightsApiFactory implements Factory<FlightsApi> {
  private final Provider<Retrofit> retrofitProvider;

  private FlightModule_Companion_ProvideFlightsApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public FlightsApi get() {
    return provideFlightsApi(retrofitProvider.get());
  }

  public static FlightModule_Companion_ProvideFlightsApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new FlightModule_Companion_ProvideFlightsApiFactory(retrofitProvider);
  }

  public static FlightsApi provideFlightsApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(FlightModule.Companion.provideFlightsApi(retrofit));
  }
}
