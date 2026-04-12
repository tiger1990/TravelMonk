package com.travelmonk.feature.services.di

import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.feature.services.ui.ServicesScreen
import com.travelmonk.feature.servicesapi.navigation.ServiceNavKey
import com.travelmonk.feature.servicesapi.navigator.ServiceNavigator
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
        navigator: ServiceNavigator
    ): NavEntryInstaller = {
        entry<ServiceNavKey.Root> {
            ServicesScreen(navigator = navigator)
        }
    }
}
