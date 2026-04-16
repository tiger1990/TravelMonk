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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.travelmonk.core.design.system.theme.TravelMonkTheme
import com.travelmonk.core.tokens.TravelMonkIcons
import com.travelmonk.feature.stays.mvi.*
import com.travelmonk.feature.staysapi.navigation.StayNavKey
import com.travelmonk.feature.staysapi.navigator.StayNavigator

// Stateful entry point
@Composable
fun StaySearchScreen(
    navigator: StayNavigator,
    viewModel: StayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StayEffect.NavigateToResults -> navigator.navigateTo(StayNavKey.Results(effect.location))
            }
        }
    }

    StaySearchContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

// Stateless content — previewable without ViewModel
@Composable
fun StaySearchContent(
    state: StaySearchState,
    onIntent: (StayIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TravelMonkTheme.colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TravelMonkTheme.colors.secondary, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .padding(top = 40.dp, start = TravelMonkTheme.spacing.large, end = TravelMonkTheme.spacing.large, bottom = 60.dp)
        ) {
            Column {
                Text(
                    text = "Find your best\nstaying place",
                    color = TravelMonkTheme.colors.onPrimary,
                    style = TravelMonkTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(TravelMonkTheme.spacing.large))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(TravelMonkTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(TravelMonkIcons.Search), contentDescription = null, tint = TravelMonkTheme.colors.grayText)
                        Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.small))
                        Text("Search for hotel, apartment...", color = TravelMonkTheme.colors.grayText, style = TravelMonkTheme.typography.labelMedium)
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(TravelMonkTheme.spacing.large)) {
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
                                .size(60.dp)
                                .background(
                                    if (isSelected) TravelMonkTheme.colors.secondary else TravelMonkTheme.colors.surface,
                                    RoundedCornerShape(16.dp)
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
                                tint = if (isSelected) TravelMonkTheme.colors.onPrimary else TravelMonkTheme.colors.secondary
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
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = TravelMonkTheme.typography.titleLarge)
        Text(text = "View all", color = TravelMonkTheme.colors.secondary, style = TravelMonkTheme.typography.labelMedium)
    }
}

@Composable
fun DestinationChip(city: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = TravelMonkTheme.spacing.medium, vertical = TravelMonkTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(TravelMonkIcons.LocationOn), contentDescription = null, modifier = Modifier.size(16.dp), tint = TravelMonkTheme.colors.secondary)
            Spacer(modifier = Modifier.width(TravelMonkTheme.spacing.small))
            Text(text = city, style = TravelMonkTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StayItem(title: String, location: String, price: String, rating: String, imageUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TravelMonkTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .padding(TravelMonkTheme.spacing.small)
                        .background(TravelMonkTheme.colors.surface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(TravelMonkIcons.Star), contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = rating, style = TravelMonkTheme.typography.caption, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.padding(TravelMonkTheme.spacing.medium)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = title, style = TravelMonkTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = "$price / night", color = TravelMonkTheme.colors.secondary, style = TravelMonkTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(TravelMonkIcons.LocationOn), contentDescription = null, modifier = Modifier.size(14.dp), tint = TravelMonkTheme.colors.grayText)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = location, style = TravelMonkTheme.typography.caption, color = TravelMonkTheme.colors.grayText)
                }
            }
        }
    }
}

@Preview(name = "Stays – Light", showBackground = true)
@Preview(name = "Stays – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StaySearchContentPreview() {
    TravelMonkTheme {
        StaySearchContent(
            state = StaySearchState(),
            onIntent = {}
        )
    }
}
