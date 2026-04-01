package com.travelmonk.feature.services.di;

import com.travelmonk.feature.services.data.remote.ServicesApi;
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
public final class ServiceModule_Companion_ProvideServicesApiFactory implements Factory<ServicesApi> {
  private final Provider<Retrofit> retrofitProvider;

  private ServiceModule_Companion_ProvideServicesApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ServicesApi get() {
    return provideServicesApi(retrofitProvider.get());
  }

  public static ServiceModule_Companion_ProvideServicesApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new ServiceModule_Companion_ProvideServicesApiFactory(retrofitProvider);
  }

  public static ServicesApi provideServicesApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(ServiceModule.Companion.provideServicesApi(retrofit));
  }
}
