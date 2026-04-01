package com.travelmonk.feature.stays.ui;

import com.travelmonk.feature.stays.domain.repository.StayRepository;
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
public final class StayViewModel_Factory implements Factory<StayViewModel> {
  private final Provider<StayRepository> stayRepositoryProvider;

  private StayViewModel_Factory(Provider<StayRepository> stayRepositoryProvider) {
    this.stayRepositoryProvider = stayRepositoryProvider;
  }

  @Override
  public StayViewModel get() {
    return newInstance(stayRepositoryProvider.get());
  }

  public static StayViewModel_Factory create(Provider<StayRepository> stayRepositoryProvider) {
    return new StayViewModel_Factory(stayRepositoryProvider);
  }

  public static StayViewModel newInstance(StayRepository stayRepository) {
    return new StayViewModel(stayRepository);
  }
}
