package com.travelmonk.feature.transport.provider

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.travelmonk.core.design.system.theme.TravelMonkTheme
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

@Preview(name = "Bus – Light", showBackground = true)
@Preview(name = "Bus – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BusSearchScreenPreview() {
    TravelMonkTheme {
        BusSearchScreen()
    }
}

@Composable
fun BusSearchScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background)
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
            accentColor = TravelMonkTheme.colors.primary
        )
    }
}