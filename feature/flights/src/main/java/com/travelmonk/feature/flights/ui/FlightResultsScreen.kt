package com.travelmonk.feature.flights.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator

private val previewFlights = listOf(
    FlightResultItem("Air Indigo",    "08:30", "11:45", "3h 15m", "$120"),
    FlightResultItem("Sky Jet",       "10:15", "13:30", "3h 15m", "$145"),
    FlightResultItem("Star Airways",  "14:00", "17:15", "3h 15m", "$110")
)

@Composable
fun FlightResultsScreen(
    from: String,
    to: String,
    navigator: FlightNavigator,
    onBook: (String) -> Unit
) {
    FlightResultsContent(
        from = from,
        to = to,
        flights = previewFlights,
        onBack = navigator::back,
        onBook = onBook
    )
}

@Composable
fun FlightResultsContent(
    from: String,
    to: String,
    flights: List<FlightResultItem>,
    onBack: () -> Unit,
    onBook: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(TravelMonkTheme.colors.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TravelMonkTheme.colors.primary, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(painter = painterResource(TravelMonkIcons.ArrowBack), contentDescription = null, tint = TravelMonkTheme.colors.onPrimary)
                }
                Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.medium))
                Column {
                    Text(text = "$from → $to", color = TravelMonkTheme.colors.onPrimary, style = TravelMonkTheme.typography.titleLarge)
                    Text(text = "Oct 24 • 1 Passenger", color = TravelMonkTheme.colors.onPrimary.copy(alpha = 0.8f), style = TravelMonkTheme.typography.caption)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(TravelMonkTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)
        ) {
            items(flights, key = { it.airline }) { flight ->
                FlightTicketCard(flight, onBook)
            }
        }
    }
}

data class FlightResultItem(val airline: String, val dep: String, val arr: String, val duration: String, val price: String)

@Composable
fun FlightTicketCard(flight: FlightResultItem, onBook: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(TravelMonkTheme.spacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(TravelMonkTheme.colors.grayText.copy(alpha = 0.2f), RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.medium))
                Text(text = flight.airline, style = TravelMonkTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = flight.price, color = TravelMonkTheme.colors.primary, style = TravelMonkTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = flight.dep, style = TravelMonkTheme.typography.headlineMedium)
                    Text(text = "Dep", color = TravelMonkTheme.colors.grayText, style = TravelMonkTheme.typography.caption)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = flight.duration, style = TravelMonkTheme.typography.caption, color = TravelMonkTheme.colors.grayText)
                    Icon(painter = painterResource(TravelMonkIcons.FlightTakeoff), contentDescription = null, tint = TravelMonkTheme.colors.primary.copy(alpha = 0.5f))
                    HorizontalDivider(modifier = Modifier.width(60.dp), color = TravelMonkTheme.colors.primary.copy(alpha = 0.3f))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = flight.arr, style = TravelMonkTheme.typography.headlineMedium)
                    Text(text = "Arr", color = TravelMonkTheme.colors.grayText, style = TravelMonkTheme.typography.caption)
                }
            }

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))

            Button(
                onClick = { onBook(flight.airline) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TravelMonkTheme.colors.primary.copy(alpha = 0.1f), contentColor = TravelMonkTheme.colors.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Select Flight", style = TravelMonkTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(name = "Flight Results – Light", showBackground = true)
@Preview(name = "Flight Results – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FlightResultsContentPreview() {
    TravelMonkTheme {
        FlightResultsContent(
            from = "San Francisco",
            to = "New York",
            flights = previewFlights,
            onBack = {},
            onBook = {}
        )
    }
}
