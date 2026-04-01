package com.travelmonk.feature.bookings.ui;

import com.travelmonk.feature.bookings.domain.repository.BookingRepository;
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
public final class BookingViewModel_Factory implements Factory<BookingViewModel> {
  private final Provider<BookingRepository> bookingRepositoryProvider;

  private BookingViewModel_Factory(Provider<BookingRepository> bookingRepositoryProvider) {
    this.bookingRepositoryProvider = bookingRepositoryProvider;
  }

  @Override
  public BookingViewModel get() {
    return newInstance(bookingRepositoryProvider.get());
  }

  public static BookingViewModel_Factory create(
      Provider<BookingRepository> bookingRepositoryProvider) {
    return new BookingViewModel_Factory(bookingRepositoryProvider);
  }

  public static BookingViewModel newInstance(BookingRepository bookingRepository) {
    return new BookingViewModel(bookingRepository);
  }
}
