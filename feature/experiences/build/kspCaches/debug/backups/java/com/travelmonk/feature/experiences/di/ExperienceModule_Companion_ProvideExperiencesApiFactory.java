package com.travelmonk.feature.experiences.di;

import com.travelmonk.feature.experiences.data.remote.ExperiencesApi;
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
public final class ExperienceModule_Companion_ProvideExperiencesApiFactory implements Factory<ExperiencesApi> {
  private final Provider<Retrofit> retrofitProvider;

  private ExperienceModule_Companion_ProvideExperiencesApiFactory(
      Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ExperiencesApi get() {
    return provideExperiencesApi(retrofitProvider.get());
  }

  public static ExperienceModule_Companion_ProvideExperiencesApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new ExperienceModule_Companion_ProvideExperiencesApiFactory(retrofitProvider);
  }

  public static ExperiencesApi provideExperiencesApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(ExperienceModule.Companion.provideExperiencesApi(retrofit));
  }
}
