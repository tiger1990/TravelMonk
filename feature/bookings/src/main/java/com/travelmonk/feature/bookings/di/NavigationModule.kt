package com.travelmonk.feature.bookings.di

import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.feature.bookings.ui.BookingConfirmationScreen
import com.travelmonk.feature.bookings.ui.MyBookingsScreen
import com.travelmonk.feature.bookingsapi.navigation.BookingNavKey
import com.travelmonk.feature.bookingsapi.navigator.BookingNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object NavigationModule {

    @Provides
    @IntoSet
    @ActivityRetainedScoped
    fun provideEntryProviderInstaller(navigator: BookingNavigator): NavEntryInstaller = {
        entry<BookingNavKey.Root> {
            MyBookingsScreen(navigator = navigator)
        }
        entry<BookingNavKey.Confirmation> { key ->
            BookingConfirmationScreen(
                type = key.type,
                title = key.title,
                onDone = { navigator.navigateToMyBookings() }
            )
        }
    }
}
