package com.travelmonk.feature.flights.ui

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.flights.mvi.*
import com.travelmonk.feature.flightsapi.navigation.FlightNavKey
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator

// Stateful entry point
@Composable
fun FlightSearchScreen(
    navigator: FlightNavigator,
    viewModel: FlightViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FlightEffect.NavigateToResults ->
                    navigator.navigateTo(FlightNavKey.Results(effect.from, effect.to))
                is FlightEffect.ShowError -> { /* Handle error */ }
            }
        }
    }

    FlightSearchContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = navigator::back
    )
}

// Stateless content — previewable without ViewModel
@Composable
fun FlightSearchContent(
    state: FlightSearchState,
    onIntent: (FlightIntent) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TravelMonkTheme.colors.primary, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 40.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(painter = painterResource(TravelMonkIcons.ArrowBack), contentDescription = null, tint = TravelMonkTheme.colors.onPrimary)
                    }
                    Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.medium))
                    Text("Search Flights", color = TravelMonkTheme.colors.onPrimary, style = TravelMonkTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))
                Text("Where are you\ngoing today?", color = TravelMonkTheme.colors.onPrimary, style = TravelMonkTheme.typography.headlineMedium)
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .offset(y = (-30).dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    TripTypeSelector(
                        selectedType = state.tripType,
                        onTypeSelected = { onIntent(FlightIntent.ChangeTripType(it)) }
                    )

                    Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            LocationField(label = "From", city = state.fromCity, code = state.fromCode, icon = TravelMonkIcons.FlightTakeoff)
                            HorizontalDivider(modifier = Modifier.padding(vertical = TravelMonkTheme.spacing.small))
                            LocationField(label = "To", city = state.toCity, code = state.toCode, icon = TravelMonkIcons.FlightLand)
                        }

                        FloatingActionButton(
                            onClick = { onIntent(FlightIntent.SwapCities(state.fromCity, state.toCity)) },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(44.dp),
                            shape = CircleShape,
                            containerColor = TravelMonkTheme.colors.primary,
                            contentColor = TravelMonkTheme.colors.onPrimary,
                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                        ) {
                            Icon(painter = painterResource(TravelMonkIcons.Swap_Vert),
                                contentDescription = "Swap",
                                tint = TravelMonkTheme.colors.onPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        SearchInfoCard(label = "Departure", value = state.departureDate, icon = TravelMonkIcons.CalendarToday, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.medium))
                        SearchInfoCard(label = "Passengers", value = "${state.passengers} Adult", icon = TravelMonkIcons.Person, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))

                    Button(
                        onClick = { onIntent(FlightIntent.SearchFlights) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TravelMonkTheme.colors.primary)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TravelMonkTheme.colors.onPrimary)
                        } else {
                            Text("Search Flights", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripTypeSelector(selectedType: TripType, onTypeSelected: (TripType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F4F8)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TripType.entries.forEach { type ->
            val isSelected = selectedType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTypeSelected(type) }
                    .background(if (isSelected) TravelMonkTheme.colors.primary else Color.Transparent)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.name.replace("_", " "),
                    color = if (isSelected) TravelMonkTheme.colors.onPrimary else TravelMonkTheme.colors.grayText,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun LocationField(label: String, city: String, code: String, @DrawableRes icon: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = TravelMonkTheme.colors.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.medium))
        Column {
            Text(label, color = TravelMonkTheme.colors.grayText, style = TravelMonkTheme.typography.caption)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(city, style = TravelMonkTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.small))
                Text("($code)", style = TravelMonkTheme.typography.labelMedium, color = TravelMonkTheme.colors.grayText)
            }
        }
    }
}

@Composable
fun SearchInfoCard(label: String, value: String, @DrawableRes icon: Int, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFF1F4F8), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(label, color = TravelMonkTheme.colors.grayText, style = TravelMonkTheme.typography.caption)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(16.dp), tint = TravelMonkTheme.colors.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(value, style = TravelMonkTheme.typography.labelMedium)
        }
    }
}

@Preview(name = "Flight Search – Light", showBackground = true)
@Preview(name = "Flight Search – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FlightSearchContentPreview() {
    TravelMonkTheme {
        FlightSearchContent(
            state = FlightSearchState(),
            onIntent = {},
            onBack = {}
        )
    }
}
