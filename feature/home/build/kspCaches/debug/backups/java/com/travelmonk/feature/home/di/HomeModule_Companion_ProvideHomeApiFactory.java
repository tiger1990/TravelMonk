package com.travelmonk.feature.home.di;

import com.travelmonk.feature.home.data.remote.HomeApi;
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
public final class HomeModule_Companion_ProvideHomeApiFactory implements Factory<HomeApi> {
  private final Provider<Retrofit> retrofitProvider;

  private HomeModule_Companion_ProvideHomeApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public HomeApi get() {
    return provideHomeApi(retrofitProvider.get());
  }

  public static HomeModule_Companion_ProvideHomeApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new HomeModule_Companion_ProvideHomeApiFactory(retrofitProvider);
  }

  public static HomeApi provideHomeApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(HomeModule.Companion.provideHomeApi(retrofit));
  }
}
