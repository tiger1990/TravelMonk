package com.travelmonk.feature.bookings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.travelmonk.feature.bookings.R
import android.content.res.Configuration
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.model.BookingType
import com.travelmonk.core.tokens.TravelMonkIcons

@Composable
fun BookingConfirmationScreen(
    type: BookingType,
    title: String,
    onDone: () -> Unit
) {
    BookingConfirmationContent(type = type, title = title, onDone = onDone)
}

@Composable
fun BookingConfirmationContent(
    type: BookingType,
    title: String,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(TravelMonkIcons.CheckCircle),
                contentDescription = null,
                modifier = Modifier.size(TravelMonkTheme.dimensions.iconExtraLarge),
                tint = TravelMonkTheme.colors.tertiary
            )
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.medium))
            Text(
                text = stringResource(R.string.bookings_booked_successfully, type.name.lowercase().replaceFirstChar { it.uppercase() }),
                style = TravelMonkTheme.typography.titleLarge,
                color = TravelMonkTheme.colors.onBackground
            )
            Text(
                text = stringResource(R.string.bookings_confirmed_with, title),
                style = TravelMonkTheme.typography.bodyLarge,
                color = TravelMonkTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.extraLarge))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = TravelMonkTheme.colors.primary)
            ) {
                Text(stringResource(R.string.bookings_view_my_bookings), color = TravelMonkTheme.colors.onPrimary)
            }
        }
    }
}

@Preview(name = "Booking Confirmation – Light", showBackground = true)
@Preview(
    name = "Booking Confirmation – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun BookingConfirmationContentPreview() {
    TravelMonkTheme {
        BookingConfirmationContent(
            type = BookingType.FLIGHT,
            title = "Air Indigo",
            onDone = {}
        )
    }
}
