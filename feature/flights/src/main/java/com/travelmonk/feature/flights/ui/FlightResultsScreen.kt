package com.travelmonk.feature.flights.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.travelmonk.feature.flights.R
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.theme.TravelMonkComponentPreviews
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.design.system.theme.TravelMonkThemeWrapper
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.core.ui.TravelMonkTopBar
import com.travelmonk.core.ui.utils.LogScreenLifecycle
import com.travelmonk.core.ui.utils.TravelMonkSnackBarHost
import com.travelmonk.feature.flights.domain.model.Flight
import com.travelmonk.feature.flights.mvi.FlightEffect
import com.travelmonk.feature.flights.mvi.FlightIntent
import com.travelmonk.feature.flightsapi.navigator.FlightNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// Stateful entry point — only this touches hiltViewModel()
@Composable
fun FlightResultsScreen(
    from: String,
    to: String,
    navigator: FlightNavigator,
    onBook: (String) -> Unit,
    viewModel: FlightViewModel = hiltViewModel()
) {
    LogScreenLifecycle("FlightResultsScreen")

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(from, to) {
        viewModel.onIntent(FlightIntent.LoadResults(from, to))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FlightEffect.ShowError -> snackBarHostState.showSnackbar(effect.message)
                else -> Unit
            }
        }
    }

    FlightResultsContent(
        from = from,
        to = to,
        flights = state.flights,
        isLoading = state.isLoading,
        onBack = navigator::back,
        onBook = onBook,
        snackBarHostState = snackBarHostState
    )
}

// Stateless content — owns the Scaffold, previewable without ViewModel
@Composable
fun FlightResultsContent(
    from: String,
    to: String,
    flights: ImmutableList<Flight>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onBook: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TravelMonkTopBar(
                title = {
                    Column {
                        Text(
                            text = "$from → $to",
                            color = TravelMonkTheme.colors.onPrimary,
                            style = TravelMonkTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(R.string.feature_flights_date_passenger_placeholder),
                            color = TravelMonkTheme.colors.onPrimary.copy(alpha = 0.8f),
                            style = TravelMonkTheme.typography.caption
                        )
                    }
                },
                containerColor = TravelMonkTheme.colors.primary,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(TravelMonkIcons.ArrowBack),
                            contentDescription = stringResource(R.string.feature_flights_navigate_back_cd),
                            tint = TravelMonkTheme.colors.onPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { TravelMonkSnackBarHost(snackBarHostState) },
        containerColor = TravelMonkTheme.colors.background,
        modifier = modifier
    ) { innerPadding ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TravelMonkTheme.colors.primary)
            }
            flights.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.feature_flights_no_results),
                    style = TravelMonkTheme.typography.bodyLarge,
                    color = TravelMonkTheme.colors.onSurfaceVariant
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(TravelMonkTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)
            ) {
                items(flights, key = { it.id }) { flight ->
                    FlightTicketCard(flight, onBook)
                }
            }
        }
    }
}

@Composable
fun FlightTicketCard(flight: Flight, onBook: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TravelMonkTheme.radius.medium),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = TravelMonkTheme.dimensions.cardElevation)
    ) {
        Column(modifier = Modifier.padding(TravelMonkTheme.spacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            TravelMonkTheme.colors.onSurfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(TravelMonkTheme.radius.extraSmall)
                        )
                )
                Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.medium))
                Text(
                    text = flight.airline,
                    style = TravelMonkTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = flight.price,
                    color = TravelMonkTheme.colors.primary,
                    style = TravelMonkTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = flight.departureTime, style = TravelMonkTheme.typography.headlineMedium)
                    Text(text = stringResource(R.string.feature_flights_departure_abbr), color = TravelMonkTheme.colors.onSurfaceVariant, style = TravelMonkTheme.typography.caption)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = flight.duration, style = TravelMonkTheme.typography.caption, color = TravelMonkTheme.colors.onSurfaceVariant)
                    Icon(painter = painterResource(TravelMonkIcons.FlightTakeoff), contentDescription = null, tint = TravelMonkTheme.colors.primary.copy(alpha = 0.5f))
                    HorizontalDivider(modifier = Modifier.width(60.dp), color = TravelMonkTheme.colors.primary.copy(alpha = 0.3f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = flight.arrivalTime, style = TravelMonkTheme.typography.headlineMedium)
                    Text(text = stringResource(R.string.feature_flights_arrival_abbr), color = TravelMonkTheme.colors.onSurfaceVariant, style = TravelMonkTheme.typography.caption)
                }
            }

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))

            Button(
                onClick = { onBook(flight.airline) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TravelMonkTheme.colors.primary.copy(alpha = 0.1f),
                    contentColor = TravelMonkTheme.colors.primary
                ),
                shape = RoundedCornerShape(TravelMonkTheme.radius.small)
            ) {
                Text(stringResource(R.string.feature_flights_select_button), style = TravelMonkTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private val previewFlights = persistentListOf(
    Flight("1", "Air Indigo",   "08:30", "11:45", "3h 15m", "$120", "SFO", "JFK"),
    Flight("2", "Sky Jet",      "10:15", "13:30", "3h 15m", "$145", "SFO", "JFK"),
    Flight("3", "Star Airways", "14:00", "17:15", "3h 15m", "$110", "SFO", "JFK")
)

@TravelMonkComponentPreviews
@PreviewWrapper(TravelMonkThemeWrapper::class)
@Composable
private fun FlightResultsContentPreview() {
    FlightResultsContent(
        from = "San Francisco",
        to = "New York",
        flights = previewFlights,
        isLoading = false,
        onBack = {},
        onBook = {}
    )
}
