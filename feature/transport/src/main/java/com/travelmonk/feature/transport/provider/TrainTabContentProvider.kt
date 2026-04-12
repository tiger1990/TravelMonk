package com.travelmonk.feature.transport.provider

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.feature.transportapi.TransportTab
import com.travelmonk.feature.transportapi.TransportTabContentProvider
import com.travelmonk.feature.transport.ui.TransportSearchCard
import javax.inject.Inject

class TrainTabContentProvider @Inject constructor() : TransportTabContentProvider {
    override val tab: TransportTab = TransportTab.TRAIN
    @Composable
    override fun Content() {
        TrainSearchScreen()
    }
}

@Composable
fun TrainSearchScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TravelMonkTheme.spacing.large)
    ) {
        TransportSearchCard(
            title = "Book Train Tickets",
            fromLabel = "Origin Station",
            fromValue = "New Delhi (NDLS)",
            toLabel = "Destination Station",
            toValue = "Mumbai Central (MMCT)",
            dateLabel = "Journey Date",
            dateValue = "Oct 28, 2024",
            buttonText = "Search Trains",
            accentColor = TravelMonkTheme.colors.secondary
        )
    }
}