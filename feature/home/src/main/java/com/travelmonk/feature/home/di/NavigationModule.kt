package com.travelmonk.feature.home.di

import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.feature.home.ui.HomeScreen
import com.travelmonk.feature.homeapi.navigation.HomeNavKey
import com.travelmonk.feature.homeapi.navigator.HomeNavigator
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
        navigator: HomeNavigator
    ): NavEntryInstaller = {
        entry<HomeNavKey.Root> {
            HomeScreen(navigator = navigator)
        }
    }
}
