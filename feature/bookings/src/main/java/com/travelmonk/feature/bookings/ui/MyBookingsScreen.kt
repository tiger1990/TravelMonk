package com.travelmonk.feature.bookings.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmonk.core.designsystem.theme.TravelMonkTheme
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.bookings.mvi.BookingItem
import com.travelmonk.feature.bookings.mvi.BookingState
import com.travelmonk.feature.bookings.mvi.BookingType

// Stateful entry point
@Composable
fun MyBookingsScreen(
    viewModel: BookingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    MyBookingsContent(state = state)
}

// Stateless content — previewable without ViewModel
@Composable
fun MyBookingsContent(state: BookingState) {
    Column(modifier = Modifier.fillMaxSize().background(TravelMonkTheme.colors.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TravelMonkTheme.dimensions.headerHeight)
                .background(TravelMonkTheme.colors.primary, RoundedCornerShape(bottomStart = TravelMonkTheme.radius.large, bottomEnd = TravelMonkTheme.radius.large))
                .padding(TravelMonkTheme.spacing.large),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(text = "My Bookings", color = TravelMonkTheme.colors.onPrimary, style = TravelMonkTheme.typography.titleLarge)
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                BookingType.FLIGHT  -> TravelMonkIcons.Flight
                BookingType.HOTEL   -> TravelMonkIcons.Hotel
                BookingType.SERVICE -> TravelMonkIcons.CleaningServices
                BookingType.PACKAGE -> TravelMonkIcons.Inventory
                else                -> TravelMonkIcons.ConfirmationNumber
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
                Text(text = booking.date, style = TravelMonkTheme.typography.caption, color = TravelMonkTheme.colors.grayText)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = booking.status,
                    style = TravelMonkTheme.typography.caption,
                    color = if (booking.status == "Confirmed") Color(0xFF4CAF50) else Color(0xFFFFA000),
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = booking.price, style = TravelMonkTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Preview(name = "Bookings – Light", showBackground = true)
@Preview(name = "Bookings – Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MyBookingsContentPreview() {
    TravelMonkTheme {
        MyBookingsContent(
            state = BookingState(
                bookings = listOf(
                    BookingItem("1", BookingType.FLIGHT, "SFO → JFK", "Oct 24, 2024", "Confirmed", "$120"),
                    BookingItem("2", BookingType.HOTEL, "The Grand Oberoi", "Nov 5, 2024", "Pending", "$240")
                )
            )
        )
    }
}
