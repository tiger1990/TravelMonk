package com.travelmonk.di

import com.travelmonk.feature.flights.navigator.FlightNavigator
import com.travelmonk.feature.stays.navigator.StayNavigator
import com.travelmonk.feature.services.navigator.ServiceNavigator
import com.travelmonk.feature.experiences.navigator.ExperienceNavigator
import com.travelmonk.feature.home.navigator.HomeNavigator
import com.travelmonk.navigation.GlobalNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun bindFlightNavigator(globalNavigator: GlobalNavigator): FlightNavigator

    @Binds
    abstract fun bindStayNavigator(globalNavigator: GlobalNavigator): StayNavigator

    @Binds
    abstract fun bindServiceNavigator(globalNavigator: GlobalNavigator): ServiceNavigator

    @Binds
    abstract fun bindExperienceNavigator(globalNavigator: GlobalNavigator): ExperienceNavigator

    @Binds
    abstract fun bindHomeNavigator(globalNavigator: GlobalNavigator): HomeNavigator
}
