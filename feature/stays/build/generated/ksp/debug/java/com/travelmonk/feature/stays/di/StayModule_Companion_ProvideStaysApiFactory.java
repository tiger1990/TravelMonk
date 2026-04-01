package com.travelmonk.feature.stays.di;

import com.travelmonk.feature.stays.data.remote.StaysApi;
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
public final class StayModule_Companion_ProvideStaysApiFactory implements Factory<StaysApi> {
  private final Provider<Retrofit> retrofitProvider;

  private StayModule_Companion_ProvideStaysApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public StaysApi get() {
    return provideStaysApi(retrofitProvider.get());
  }

  public static StayModule_Companion_ProvideStaysApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new StayModule_Companion_ProvideStaysApiFactory(retrofitProvider);
  }

  public static StaysApi provideStaysApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(StayModule.Companion.provideStaysApi(retrofit));
  }
}
