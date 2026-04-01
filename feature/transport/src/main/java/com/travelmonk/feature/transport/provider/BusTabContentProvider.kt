package com.travelmonk.feature.transport.provider

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.travelmonk.core.designsystem.theme.TravelMonkTheme
import com.travelmonk.feature.transportapi.TransportTab
import com.travelmonk.feature.transportapi.TransportTabContentProvider
import com.travelmonk.feature.transport.ui.TransportSearchCard
import javax.inject.Inject

class BusTabContentProvider @Inject constructor() : TransportTabContentProvider {
    override val tab: TransportTab = TransportTab.BUS

    @Composable
    override fun Content() {
        BusSearchScreen()
    }
}

@Composable
fun BusSearchScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TravelMonkTheme.spacing.large)
    ) {
        TransportSearchCard(
            title = "Book Bus Tickets",
            fromLabel = "From City",
            fromValue = "Bangalore",
            toLabel = "To City",
            toValue = "Goa",
            dateLabel = "Travel Date",
            dateValue = "Oct 26, 2024",
            buttonText = "Search Buses",
            accentColor = Color(0xFF4CAF50)
        )
    }
}