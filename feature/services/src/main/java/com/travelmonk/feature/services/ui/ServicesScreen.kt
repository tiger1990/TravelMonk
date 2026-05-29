package com.travelmonk.feature.services.ui

import android.content.res.Configuration
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.model.BookingType
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.core.ui.TravelMonkTopBar
import com.travelmonk.core.ui.utils.LogScreenLifecycle
import com.travelmonk.feature.services.R
import com.travelmonk.feature.services.domain.model.TravelService
import com.travelmonk.feature.services.mvi.ServicesEffect
import com.travelmonk.feature.services.mvi.ServicesIntent
import com.travelmonk.feature.servicesapi.navigator.ServiceNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ServicesScreen(
    navigator: ServiceNavigator,
    viewModel: ServicesViewModel = hiltViewModel()
) {
    LogScreenLifecycle("ServicesScreen")

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ServicesEffect.NavigateToBooking ->
                    navigator.navigateToBookingConfirmation(BookingType.SERVICE, effect.service.name)
            }
        }
    }

    Scaffold(
        topBar = {
            TravelMonkTopBar(
                title = {
                    Text(
                        text = stringResource(R.string.feature_services_home_services),
                        color = TravelMonkTheme.colors.onPrimary,
                        style = TravelMonkTheme.typography.titleLarge
                    )
                },
                containerColor = TravelMonkTheme.colors.primary
            )
        },
        containerColor = TravelMonkTheme.colors.background
    ) { innerPadding ->
        ServicesContent(
            services = state.services,
            onIntent = viewModel::onIntent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun ServicesContent(
    services: ImmutableList<TravelService>,
    onIntent: (ServicesIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background),
        contentPadding = PaddingValues(TravelMonkTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)
    ) {
        items(services, key = { it.id }) { service ->
            ServiceCard(service) { onIntent(ServicesIntent.SelectService(service)) }
        }
    }
}

@Composable
fun ServiceCard(service: TravelService, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(TravelMonkTheme.dimensions.imageCardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(TravelMonkTheme.radius.medium),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = TravelMonkTheme.dimensions.cardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(TravelMonkTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(TravelMonkIcons.byName(service.iconName)),
                contentDescription = null,
                modifier = Modifier.size(TravelMonkTheme.dimensions.iconLarge),
                tint = TravelMonkTheme.colors.primary
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
                color = TravelMonkTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "Services – Light", showBackground = true)
@Preview(name = "Services – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ServicesContentPreview() {
    TravelMonkTheme {
        ServicesContent(
            services = persistentListOf(
                TravelService("1", "Maid & Helper", "cleaning_services", "Daily cleaning & domestic help"),
                TravelService("2", "Site Visit", "real_estate_agent", "Property & landmark tours"),
                TravelService("3", "Tour Guide", "person_search", "Expert local storytellers"),
                TravelService("4", "Local Support", "support_agent", "24/7 travel assistance"),
            ),
            onIntent = {}
        )
    }
}
