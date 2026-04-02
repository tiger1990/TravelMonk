package com.travelmonk.feature.experiences.di

import com.travelmonk.core.navigation.NavKeyHandler
import com.travelmonk.feature.experiences.navigation.ExperienceNavKeyHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ExperienceNavHandlerModule {

    @Binds
    @IntoSet
    abstract fun bindExperienceHandler(impl: ExperienceNavKeyHandler): NavKeyHandler
}