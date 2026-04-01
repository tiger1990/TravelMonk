package com.travelmonk.feature.stays.data.repository;

import com.travelmonk.feature.stays.data.remote.StaysApi;
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
public final class StayRepositoryImpl_Factory implements Factory<StayRepositoryImpl> {
  private final Provider<StaysApi> staysApiProvider;

  private StayRepositoryImpl_Factory(Provider<StaysApi> staysApiProvider) {
    this.staysApiProvider = staysApiProvider;
  }

  @Override
  public StayRepositoryImpl get() {
    return newInstance(staysApiProvider.get());
  }

  public static StayRepositoryImpl_Factory create(Provider<StaysApi> staysApiProvider) {
    return new StayRepositoryImpl_Factory(staysApiProvider);
  }

  public static StayRepositoryImpl newInstance(StaysApi staysApi) {
    return new StayRepositoryImpl(staysApi);
  }
}
