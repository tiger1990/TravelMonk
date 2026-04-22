package com.travelmonk.feature.bookings.ui

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.color.WarningAmber
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.model.BookingType
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.core.ui.TravelMonkTopBar
import com.travelmonk.feature.bookings.domain.model.BookingItem
import com.travelmonk.feature.bookings.mvi.BookingState
import com.travelmonk.feature.bookingsapi.navigator.BookingNavigator

// Stateful entry point
@Composable
fun MyBookingsScreen(
    navigator: BookingNavigator,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TravelMonkTopBar(
                title = { Text("My Bookings") },
                containerColor = TravelMonkTheme.colors.primary
            )
        },
        containerColor = TravelMonkTheme.colors.background
    ) { innerPadding ->
        MyBookingsContent(state = state, modifier = Modifier.padding(innerPadding))
    }
}

// Stateless content — previewable without ViewModel
@Composable
fun MyBookingsContent(
    state: BookingState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background)
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TravelMonkTheme.colors.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(TravelMonkTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)
            ) {
                items(state.bookings, key = { it.id }) { booking ->
                    BookingCard(booking)
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: BookingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TravelMonkTheme.radius.medium),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = TravelMonkTheme.dimensions.cardElevation)
    ) {
        Row(
            modifier = Modifier.padding(TravelMonkTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            @DrawableRes val icon = when (booking.type) {
                BookingType.FLIGHT   -> TravelMonkIcons.Flight
                BookingType.HOTEL    -> TravelMonkIcons.Hotel
                BookingType.SERVICE  -> TravelMonkIcons.CleaningServices
                BookingType.PACKAGE  -> TravelMonkIcons.Inventory
                BookingType.BUS,
                BookingType.TRAIN    -> TravelMonkIcons.DirectionsTransit
            }

            Box(
                modifier = Modifier
                    .size(TravelMonkTheme.dimensions.iconLarge)
                    .background(TravelMonkTheme.colors.primary.copy(alpha = 0.1f), RoundedCornerShape(TravelMonkTheme.radius.small)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = TravelMonkTheme.colors.primary)
            }

            Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = booking.title, style = TravelMonkTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(text = booking.date, style = TravelMonkTheme.typography.caption, color = TravelMonkTheme.colors.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = booking.status,
                    style = TravelMonkTheme.typography.caption,
                    color = if (booking.status == "Confirmed") TravelMonkTheme.colors.tertiary else WarningAmber,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = booking.price, style = TravelMonkTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Preview(name = "Bookings – Light", showBackground = true)
@Preview(name = "Bookings – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MyBookingsContentPreview() {
    TravelMonkTheme {
        MyBookingsContent(
            state = BookingState(
                bookings = listOf(
                    BookingItem("1", BookingType.FLIGHT, "SFO → JFK", "Oct 24, 2024", "Confirmed", "$120"),
                    BookingItem("2", BookingType.HOTEL, "The Grand Oberoi", "Nov 5, 2024", "Pending", "$240")
                )
            ),
            modifier = Modifier
        )
    }
}
