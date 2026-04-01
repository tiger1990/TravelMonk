package com.travelmonk.feature.transport.provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class TrainTabContentProvider_Factory implements Factory<TrainTabContentProvider> {
  @Override
  public TrainTabContentProvider get() {
    return newInstance();
  }

  public static TrainTabContentProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TrainTabContentProvider newInstance() {
    return new TrainTabContentProvider();
  }

  private static final class InstanceHolder {
    static final TrainTabContentProvider_Factory INSTANCE = new TrainTabContentProvider_Factory();
  }
}
