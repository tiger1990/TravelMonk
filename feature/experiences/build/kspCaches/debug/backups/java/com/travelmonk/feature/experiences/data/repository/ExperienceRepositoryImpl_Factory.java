package com.travelmonk.feature.experiences.data.repository;

import com.travelmonk.feature.experiences.data.remote.ExperiencesApi;
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
public final class ExperienceRepositoryImpl_Factory implements Factory<ExperienceRepositoryImpl> {
  private final Provider<ExperiencesApi> experiencesApiProvider;

  private ExperienceRepositoryImpl_Factory(Provider<ExperiencesApi> experiencesApiProvider) {
    this.experiencesApiProvider = experiencesApiProvider;
  }

  @Override
  public ExperienceRepositoryImpl get() {
    return newInstance(experiencesApiProvider.get());
  }

  public static ExperienceRepositoryImpl_Factory create(
      Provider<ExperiencesApi> experiencesApiProvider) {
    return new ExperienceRepositoryImpl_Factory(experiencesApiProvider);
  }

  public static ExperienceRepositoryImpl newInstance(ExperiencesApi experiencesApi) {
    return new ExperienceRepositoryImpl(experiencesApi);
  }
}
