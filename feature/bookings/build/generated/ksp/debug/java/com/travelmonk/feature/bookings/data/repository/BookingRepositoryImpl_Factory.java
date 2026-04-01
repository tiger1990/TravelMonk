package com.travelmonk.feature.bookings.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class BookingRepositoryImpl_Factory implements Factory<BookingRepositoryImpl> {
  @Override
  public BookingRepositoryImpl get() {
    return newInstance();
  }

  public static BookingRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static BookingRepositoryImpl newInstance() {
    return new BookingRepositoryImpl();
  }

  private static final class InstanceHolder {
    static final BookingRepositoryImpl_Factory INSTANCE = new BookingRepositoryImpl_Factory();
  }
}
