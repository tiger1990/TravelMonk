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

class TrainTabContentProvider @Inject constructor() : TransportTabContentProvider {
    override val tab: TransportTab = TransportTab.TRAIN
    @Composable
    override fun Content() {
        TrainSearchScreen()
    }
}

@Preview(name = "Train – Light", showBackground = true)
@Preview(name = "Train – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrainSearchScreenPreview() {
    TravelMonkTheme {
        TrainSearchScreen()
    }
}

@Composable
fun TrainSearchScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background)
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
            accentColor = TravelMonkTheme.colors.primary
        )
    }
}