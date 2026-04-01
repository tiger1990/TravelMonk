package com.travelmonk.feature.transport.ui;

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
public final class TransportViewModel_Factory implements Factory<TransportViewModel> {
  @Override
  public TransportViewModel get() {
    return newInstance();
  }

  public static TransportViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TransportViewModel newInstance() {
    return new TransportViewModel();
  }

  private static final class InstanceHolder {
    static final TransportViewModel_Factory INSTANCE = new TransportViewModel_Factory();
  }
}
