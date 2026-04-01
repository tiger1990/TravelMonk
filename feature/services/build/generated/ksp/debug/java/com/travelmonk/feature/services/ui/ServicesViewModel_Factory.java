package com.travelmonk.feature.services.ui;

import com.travelmonk.feature.services.domain.repository.ServiceRepository;
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
public final class ServicesViewModel_Factory implements Factory<ServicesViewModel> {
  private final Provider<ServiceRepository> serviceRepositoryProvider;

  private ServicesViewModel_Factory(Provider<ServiceRepository> serviceRepositoryProvider) {
    this.serviceRepositoryProvider = serviceRepositoryProvider;
  }

  @Override
  public ServicesViewModel get() {
    return newInstance(serviceRepositoryProvider.get());
  }

  public static ServicesViewModel_Factory create(
      Provider<ServiceRepository> serviceRepositoryProvider) {
    return new ServicesViewModel_Factory(serviceRepositoryProvider);
  }

  public static ServicesViewModel newInstance(ServiceRepository serviceRepository) {
    return new ServicesViewModel(serviceRepository);
  }
}
