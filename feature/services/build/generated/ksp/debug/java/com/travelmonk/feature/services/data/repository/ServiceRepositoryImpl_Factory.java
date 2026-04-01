package com.travelmonk.feature.services.data.repository;

import com.travelmonk.feature.services.data.remote.ServicesApi;
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
public final class ServiceRepositoryImpl_Factory implements Factory<ServiceRepositoryImpl> {
  private final Provider<ServicesApi> servicesApiProvider;

  private ServiceRepositoryImpl_Factory(Provider<ServicesApi> servicesApiProvider) {
    this.servicesApiProvider = servicesApiProvider;
  }

  @Override
  public ServiceRepositoryImpl get() {
    return newInstance(servicesApiProvider.get());
  }

  public static ServiceRepositoryImpl_Factory create(Provider<ServicesApi> servicesApiProvider) {
    return new ServiceRepositoryImpl_Factory(servicesApiProvider);
  }

  public static ServiceRepositoryImpl newInstance(ServicesApi servicesApi) {
    return new ServiceRepositoryImpl(servicesApi);
  }
}
