package com.travelmonk.feature.stays.ui

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.travelmonk.core.ui.utils.TravelMonkSnackBarHost
import com.travelmonk.core.ui.TravelMonkTopBar
import com.travelmonk.core.design.system.color.TravelYellow
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.stays.mvi.*
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.staysapi.navigator.StayNavigator

/**
 * Stateful entry point for the Stay Search Screen.
 */
@Composable
fun StaySearchScreen(
    navigator: StayNavigator,
    viewModel: StayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StayEffect.NavigateToResults -> navigator.navigateTo(StayNavKey.Results(effect.location))
                is StayEffect.ShowError -> snackBarHostState.showSnackbar(effect.message)
            }
        }
    }

    StaySearchScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackBarHostState = snackBarHostState
    )
}

/**
 * Stateless full-screen content for Stay Search.
 */
@Composable
fun StaySearchScreenContent(
    state: StaySearchState,
    onIntent: (StayIntent) -> Unit,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            StaySearchTopBar()
        },
        snackbarHost = { TravelMonkSnackBarHost(snackBarHostState) },
        containerColor = TravelMonkTheme.colors.background
    ) { innerPadding ->
        StaySearchListContent(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Dedicated TopBar for the Stay Search screen.
 */
@Composable
private fun StaySearchTopBar() {
    TravelMonkTopBar(
        title = {
            Text(
                text = "Find your stay",
                style = TravelMonkTheme.typography.headlineMedium,
                color = TravelMonkTheme.colors.onPrimary
            )
        },
        containerColor = TravelMonkTheme.colors.primary,
        bottomContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TravelMonkTheme.spacing.large,
                        vertical = TravelMonkTheme.spacing.medium
                    ),
                shape = RoundedCornerShape(TravelMonkTheme.radius.medium),
                colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface)
            ) {
                Row(
                    modifier = Modifier.padding(TravelMonkTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(TravelMonkIcons.Search),
                        contentDescription = null,
                        tint = TravelMonkTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(TravelMonkTheme.dimensions.iconSmall)
                    )
                    Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.small))
                    Text(
                        text = "Search for hotel, apartment...",
                        color = TravelMonkTheme.colors.onSurfaceVariant,
                        style = TravelMonkTheme.typography.labelMedium
                    )
                }
            }
        }
    )
}

/**
 * The scrollable list part of the Stay Search screen.
 */
@Composable
private fun StaySearchListContent(
    state: StaySearchState,
    onIntent: (StayIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(TravelMonkTheme.spacing.large)) {
            // Stay Type Categories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StayType.entries.forEach { type ->
                    val isSelected = state.stayType == type
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onIntent(StayIntent.ChangeStayType(type)) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(TravelMonkTheme.dimensions.categoryIconSize)
                                .background(
                                    if (isSelected) TravelMonkTheme.colors.primary else TravelMonkTheme.colors.surface,
                                    RoundedCornerShape(TravelMonkTheme.radius.medium)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            @DrawableRes val icon = when (type) {
                                StayType.HOTEL     -> TravelMonkIcons.Hotel
                                StayType.APARTMENT -> TravelMonkIcons.Apartment
                                StayType.RESORT    -> TravelMonkIcons.HolidayVillage
                            }
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = null,
                                tint = if (isSelected) TravelMonkTheme.colors.onPrimary else TravelMonkTheme.colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.small))
                        Text(
                            text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = TravelMonkTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.extraLarge))
            SectionHeader(title = "Popular Destinations")
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.medium))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(TravelMonkTheme.spacing.medium)) {
                items(listOf("Bali", "Paris", "Dubai", "New York"), key = { it }) { city ->
                    DestinationChip(city)
                }
            }

            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.extraLarge))
            SectionHeader(title = "Recommended Stays")
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.medium))

            StayItem(
                title = "The Grand Oberoi",
                location = "Bali, Indonesia",
                price = "$240",
                rating = "4.9",
                imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945"
            )
            Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.medium))
            StayItem(
                title = "Azure Apartment",
                location = "Paris, France",
                price = "$180",
                rating = "4.7",
                imageUrl = "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267"
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = TravelMonkTheme.typography.titleLarge)
        Text(text = "View all", color = TravelMonkTheme.colors.primary, style = TravelMonkTheme.typography.labelMedium)
    }
}

@Composable
private fun DestinationChip(city: String) {
    Card(
        shape = RoundedCornerShape(TravelMonkTheme.radius.medium),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = TravelMonkTheme.dimensions.cardElevation)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TravelMonkTheme.spacing.medium, 
                vertical = TravelMonkTheme.spacing.small
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(TravelMonkIcons.LocationOn), 
                contentDescription = null, 
                modifier = Modifier.size(TravelMonkTheme.dimensions.iconSmall), 
                tint = TravelMonkTheme.colors.primary
            )
            Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.small))
            Text(
                text = city, 
                style = TravelMonkTheme.typography.labelMedium, 
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StayItem(title: String, location: String, price: String, rating: String, imageUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TravelMonkTheme.radius.large),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = TravelMonkTheme.dimensions.cardElevation)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(TravelMonkTheme.dimensions.imageCardHeight),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .padding(TravelMonkTheme.spacing.small)
                        .background(
                            TravelMonkTheme.colors.surface.copy(alpha = 0.9f), 
                            RoundedCornerShape(TravelMonkTheme.radius.small)
                        )
                        .padding(horizontal = TravelMonkTheme.spacing.small, vertical = TravelMonkTheme.spacing.extraSmall)
                        .align(Alignment.TopEnd)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(TravelMonkIcons.Star), 
                            contentDescription = null, 
                            tint = TravelYellow, 
                            modifier = Modifier.size(TravelMonkTheme.dimensions.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.extraSmall))
                        Text(
                            text = rating, 
                            style = TravelMonkTheme.typography.caption, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(TravelMonkTheme.spacing.medium)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = title, style = TravelMonkTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$price / night", 
                        color = TravelMonkTheme.colors.primary, 
                        style = TravelMonkTheme.typography.bodyLarge, 
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.extraSmall))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(TravelMonkIcons.LocationOn), 
                        contentDescription = null, 
                        modifier = Modifier.size(TravelMonkTheme.dimensions.iconSmall), 
                        tint = TravelMonkTheme.colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.extraSmall))
                    Text(
                        text = location, 
                        style = TravelMonkTheme.typography.caption,
                        color = TravelMonkTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(name = "Stay Search – Light", showSystemUi = true)
@Preview(
    name = "Stay Search – Dark", 
    showSystemUi = true, 
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StaySearchScreenPreview() {
    TravelMonkTheme {
        StaySearchScreenContent(
            state = StaySearchState(),
            onIntent = {}
        )
    }
}
