package com.travelmonk.feature.stays.di

import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.feature.stays.ui.StayResultsScreen
import com.travelmonk.feature.stays.ui.StaySearchScreen
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.staysapi.navigator.StayNavigator
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
        navigator: StayNavigator
    ): NavEntryInstaller = {
        entry<StayNavKey.Search> {
            StaySearchScreen(navigator = navigator)
        }
        entry<StayNavKey.Results> { key ->
            StayResultsScreen(location = key.location, navigator = navigator)
        }
    }
}
