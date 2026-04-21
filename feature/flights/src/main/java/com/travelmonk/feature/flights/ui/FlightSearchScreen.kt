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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.core.ui.TravelMonkTopBar
import com.travelmonk.core.ui.utils.TravelMonkSnackBarHost
import com.travelmonk.feature.flights.mvi.*
import com.travelmonk.feature.flightsapi.navigation.FlightNavKey
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator

// Stateful entry point
@Composable
fun FlightSearchScreen(
    navigator: FlightNavigator,
    viewModel: FlightViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FlightEffect.NavigateToResults ->
                    navigator.navigateTo(FlightNavKey.Results(effect.from, effect.to))
                is FlightEffect.ShowError ->
                    snackBarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TravelMonkTopBar(
                title = {
                    Column {
                        Text("Search Flights", style = TravelMonkTheme.typography.titleLarge)
                        Text("Where are you going today?", style = TravelMonkTheme.typography.bodyLarge)
                    }
                },
                containerColor = TravelMonkTheme.colors.primary,
                navigationIcon = {
                    IconButton(onClick = navigator::back) {
                        Icon(
                            painter = painterResource(TravelMonkIcons.ArrowBack),
                            contentDescription = "Navigate back",
                            tint = TravelMonkTheme.colors.onPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { TravelMonkSnackBarHost(snackBarHostState) },
        containerColor = TravelMonkTheme.colors.background
    ) { innerPadding ->
        FlightSearchContent(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// Stateless content — previewable without ViewModel
@Composable
fun FlightSearchContent(
    state: FlightSearchState,
    onIntent: (FlightIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = TravelMonkTheme.spacing.large)
                .padding(top = TravelMonkTheme.spacing.large)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TravelMonkTheme.radius.large),
                colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = TravelMonkTheme.dimensions.cardElevationLarge)
            ) {
                Column(modifier = Modifier.padding(TravelMonkTheme.spacing.large)) {
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
                                .size(TravelMonkTheme.dimensions.fabSize),
                            shape = CircleShape,
                            containerColor = TravelMonkTheme.colors.primary,
                            contentColor = TravelMonkTheme.colors.onPrimary,
                            elevation = FloatingActionButtonDefaults.elevation(TravelMonkTheme.dimensions.cardElevation)
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
                            .height(TravelMonkTheme.dimensions.buttonHeight),
                        shape = RoundedCornerShape(TravelMonkTheme.radius.medium),
                        colors = ButtonDefaults.buttonColors(containerColor = TravelMonkTheme.colors.primary)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(TravelMonkTheme.dimensions.iconMedium), color = TravelMonkTheme.colors.onPrimary)
                        } else {
                            Text("Search Flights", style = TravelMonkTheme.typography.titleLarge)
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
            .clip(RoundedCornerShape(TravelMonkTheme.radius.small))
            .background(TravelMonkTheme.colors.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TripType.entries.forEach { type ->
            val isSelected = selectedType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTypeSelected(type) }
                    .background(if (isSelected) TravelMonkTheme.colors.primary else Color.Transparent)
                    .padding(vertical = TravelMonkTheme.spacing.small),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.name.replace("_", " "),
                    color = if (isSelected) TravelMonkTheme.colors.onPrimary else TravelMonkTheme.colors.grayText,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    style = TravelMonkTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
fun LocationField(label: String, city: String, code: String, @DrawableRes icon: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = TravelMonkTheme.colors.primary, modifier = Modifier.size(TravelMonkTheme.dimensions.iconMedium))
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
            .background(TravelMonkTheme.colors.surfaceVariant, RoundedCornerShape(TravelMonkTheme.radius.small))
            .padding(TravelMonkTheme.spacing.medium)
    ) {
        Text(label, color = TravelMonkTheme.colors.grayText, style = TravelMonkTheme.typography.caption)
        Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.extraSmall))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(TravelMonkTheme.dimensions.iconSmall), tint = TravelMonkTheme.colors.primary)
            Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.extraSmall))
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
            onIntent = {}
        )
    }
}
