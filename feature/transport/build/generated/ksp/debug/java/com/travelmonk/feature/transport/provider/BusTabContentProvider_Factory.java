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
public final class BusTabContentProvider_Factory implements Factory<BusTabContentProvider> {
  @Override
  public BusTabContentProvider get() {
    return newInstance();
  }

  public static BusTabContentProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BusTabContentProvider newInstance() {
    return new BusTabContentProvider();
  }

  private static final class InstanceHolder {
    static final BusTabContentProvider_Factory INSTANCE = new BusTabContentProvider_Factory();
  }
}
