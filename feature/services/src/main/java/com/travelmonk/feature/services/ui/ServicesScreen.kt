package com.travelmonk.feature.services.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.travelmonk.core.designsystem.theme.TravelMonkTheme
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.services.mvi.*
import com.travelmonk.feature.servicesapi.navigator.ServiceNavigator

private val defaultServices = listOf(
    TravelService("1", "Maid & Helper",  "cleaning_services",    "Daily cleaning & domestic help"),
    TravelService("2", "Site Visit",     "real_estate_agent",    "Property & landmark tours"),
    TravelService("3", "Tour Guide",     "person_search",        "Expert local storytellers"),
    TravelService("4", "Local Support",  "support_agent",        "24/7 travel assistance"),
    TravelService("5", "Laundry",        "local_laundry_service","Wash & Fold services"),
    TravelService("6", "Car Rental",     "directions_car",       "Self-drive or chauffeured")
)

// Stateful entry point
@Composable
fun ServicesScreen(
    navigator: ServiceNavigator,
    viewModel: ServicesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ServicesEffect.NavigateToBooking ->
                    navigator.navigateToBookingConfirmation("Service", effect.service.name)
            }
        }
    }

    ServicesContent(
        state = state,
        services = defaultServices,
        onIntent = viewModel::onIntent
    )
}

// Stateless content — previewable without ViewModel
@Composable
fun ServicesContent(
    state: ServicesState,
    services: List<TravelService>,
    onIntent: (ServicesIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(TravelMonkTheme.colors.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(TravelMonkTheme.colors.tertiary, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(TravelMonkTheme.spacing.large),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(text = "Home Services", color = TravelMonkTheme.colors.onPrimary, style = TravelMonkTheme.typography.titleLarge)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(TravelMonkTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)
        ) {
            items(services, key = { it.id }) { service ->
                ServiceCard(service) { onIntent(ServicesIntent.SelectService(service)) }
            }
        }
    }
}

@Composable
fun ServiceCard(service: TravelService, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(TravelMonkTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(TravelMonkIcons.byName(service.iconName)),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = TravelMonkTheme.colors.tertiary
            )
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.small))
            Text(
                text = service.name,
                style = TravelMonkTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = service.description,
                style = TravelMonkTheme.typography.caption,
                color = TravelMonkTheme.colors.grayText,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Preview(name = "Services – Light", showBackground = true)
@Preview(name = "Services – Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ServicesContentPreview() {
    TravelMonkTheme {
        ServicesContent(
            state = ServicesState(),
            services = defaultServices,
            onIntent = {}
        )
    }
}
