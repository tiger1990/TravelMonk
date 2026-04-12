package com.travelmonk.feature.flights.di

import com.travelmonk.core.model.BookingType
import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.feature.flights.ui.FlightResultsScreen
import com.travelmonk.feature.flights.ui.FlightSearchScreen
import com.travelmonk.feature.flightsapi.navigation.FlightNavKey
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator
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
    fun provideEntryProviderInstaller(
        navigator: FlightNavigator
    ): NavEntryInstaller = {
        entry<FlightNavKey.Search> {
            FlightSearchScreen(navigator = navigator)
        }
        entry<FlightNavKey.Results> { key ->
            FlightResultsScreen(
                from = key.from,
                to = key.to,
                navigator = navigator,
                onBook = { title ->
                    navigator.navigateToBookingConfirmation(
                        BookingType.FLIGHT, title
                    )
                }
            )
        }
    }
}
