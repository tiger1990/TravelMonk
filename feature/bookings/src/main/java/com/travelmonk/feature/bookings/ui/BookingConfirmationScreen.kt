package com.travelmonk.feature.bookings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.model.BookingType
import com.travelmonk.core.tokens.TravelMonkIcons

@Composable
fun BookingConfirmationScreen(
    type: BookingType,
    title: String,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(TravelMonkIcons.CheckCircle),
                contentDescription = null,
                modifier = Modifier.size(TravelMonkTheme.dimensions.iconExtraLarge),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.medium))
            Text(text = "${type.name.lowercase().replaceFirstChar { it.uppercase() }} Booked Successfully!", style = TravelMonkTheme.typography.titleLarge)
            Text(
                text = "Confirmed with $title",
                style = TravelMonkTheme.typography.bodyLarge,
                color = TravelMonkTheme.colors.grayText
            )
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.extraLarge))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = TravelMonkTheme.colors.primary)
            ) {
                Text("View My Bookings", color = TravelMonkTheme.colors.onPrimary)
            }
        }
    }
}
