package com.travelmonk.feature.transport.di

import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.feature.transport.ui.TransportScreen
import com.travelmonk.feature.transportapi.navigation.TransportNavKey
import com.travelmonk.feature.transportapi.navigator.TransportNavigator
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
    fun provideEntryProviderInstaller(navigator: TransportNavigator): NavEntryInstaller = {
        entry<TransportNavKey.Root> {
            TransportScreen(navigator = navigator)
        }
    }
}
