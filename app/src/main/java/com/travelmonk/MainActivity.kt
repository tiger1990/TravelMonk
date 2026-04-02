package com.travelmonk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.travelmonk.core.designsystem.theme.TravelMonkTheme
import com.travelmonk.feature.experiencesapi.navigator.ExperienceNavigator
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator
import com.travelmonk.feature.homeapi.navigator.HomeNavigator
import com.travelmonk.feature.servicesapi.navigator.ServiceNavigator
import com.travelmonk.feature.staysapi.navigator.StayNavigator
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.navigation.NavigationRegistry
import com.travelmonk.ui.TravelMonkApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // GlobalNavigator is the NavigationBus — needed to bind/unbind the composition-scoped NavigationState
    @Inject lateinit var globalNavigator: GlobalNavigator
    @Inject lateinit var navigationRegistry: NavigationRegistry

    // Feature navigators — injected as interfaces, backed by NavigationBus adapters from NavigationModule
    @Inject lateinit var homeNavigator: HomeNavigator
    @Inject lateinit var flightNavigator: FlightNavigator
    @Inject lateinit var stayNavigator: StayNavigator
    @Inject lateinit var serviceNavigator: ServiceNavigator
    @Inject lateinit var experienceNavigator: ExperienceNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelMonkTheme {
                TravelMonkApp(
                    globalNavigator = globalNavigator,
                    registry = navigationRegistry,
                    homeNavigator = homeNavigator,
                    flightNavigator = flightNavigator,
                    stayNavigator = stayNavigator,
                    serviceNavigator = serviceNavigator,
                    experienceNavigator = experienceNavigator
                )
            }
        }
    }
}
