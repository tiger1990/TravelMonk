package com.travelmonk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.travelmonk.core.designsystem.theme.TravelMonkTheme
import com.travelmonk.feature.flights.navigator.FlightNavigator
import com.travelmonk.feature.stays.navigator.StayNavigator
import com.travelmonk.feature.services.navigator.ServiceNavigator
import com.travelmonk.feature.experiences.navigator.ExperienceNavigator
import com.travelmonk.feature.home.navigator.HomeNavigator
import com.travelmonk.navigation.GlobalNavigator
import com.travelmonk.ui.TravelMonkApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var globalNavigator: GlobalNavigator

    @Inject
    lateinit var homeNavigator: HomeNavigator
    @Inject
    lateinit var flightNavigator: FlightNavigator

    @Inject
    lateinit var stayNavigator: StayNavigator

    @Inject
    lateinit var serviceNavigator: ServiceNavigator

    @Inject
    lateinit var experienceNavigator: ExperienceNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelMonkTheme {
                TravelMonkApp(
                    globalNavigator = globalNavigator,
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
