package com.travelmonk.feature.home.data.repository;

import com.travelmonk.feature.home.data.remote.HomeApi;
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
public final class HomeRepositoryImpl_Factory implements Factory<HomeRepositoryImpl> {
  private final Provider<HomeApi> homeApiProvider;

  private HomeRepositoryImpl_Factory(Provider<HomeApi> homeApiProvider) {
    this.homeApiProvider = homeApiProvider;
  }

  @Override
  public HomeRepositoryImpl get() {
    return newInstance(homeApiProvider.get());
  }

  public static HomeRepositoryImpl_Factory create(Provider<HomeApi> homeApiProvider) {
    return new HomeRepositoryImpl_Factory(homeApiProvider);
  }

  public static HomeRepositoryImpl newInstance(HomeApi homeApi) {
    return new HomeRepositoryImpl(homeApi);
  }
}
