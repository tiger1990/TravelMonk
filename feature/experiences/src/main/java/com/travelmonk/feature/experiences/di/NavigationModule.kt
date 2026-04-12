package com.travelmonk.feature.experiences.di

import com.travelmonk.core.navigation.NavEntryInstaller
import com.travelmonk.feature.experiences.ui.ExperienceDetailsScreen
import com.travelmonk.feature.experiences.ui.ExperiencesScreen
import com.travelmonk.feature.experiencesapi.navigation.ExperienceNavKey
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator
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
        navigator: ExperienceNavigator
    ): NavEntryInstaller = {
        entry<ExperienceNavKey.Root> {
            ExperiencesScreen(navigator = navigator)
        }
        entry<ExperienceNavKey.Details> { key ->
            ExperienceDetailsScreen(
                experienceId = key.experienceId,
                navigator = navigator
            )
        }
    }
}
